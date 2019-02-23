package org.odk.share.fragments;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.odk.collect.android.dao.InstancesDao;
import org.odk.collect.android.dto.Instance;
import org.odk.collect.android.provider.InstanceProviderAPI;
import org.odk.share.R;
import org.odk.share.activities.SendActivity;
import org.odk.share.adapters.TransferInstanceAdapter;
import org.odk.share.dao.TransferDao;
import org.odk.share.dto.TransferInstance;
import org.odk.share.utilities.ApplicationConstants;
import org.odk.share.utilities.ArrayUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

import static org.odk.share.activities.InstancesList.INSTANCE_IDS;
import static org.odk.share.activities.MainActivity.FORM_DISPLAY_NAME;
import static org.odk.share.activities.MainActivity.FORM_ID;
import static org.odk.share.activities.MainActivity.FORM_VERSION;

/**
 * Created by laksh on 6/27/2018.
 */

public class ReviewedInstancesFragment extends InstanceListFragment implements OnItemClickListener {


    public ReviewedInstancesFragment() {

    }

    @BindView(R.id.recyclerview)
    RecyclerView recyclerView;
    @BindView(R.id.bToggle)
    Button toggleButton;
    @BindView(R.id.bAction)
    Button sendButton;
    @BindView(R.id.empty_view)
    TextView emptyView;
    @BindView(R.id.buttonholder)
    LinearLayout buttonLayout;

    HashMap<Long, Instance> instanceMap;

    TransferInstanceAdapter transferInstanceAdapter;
    List<TransferInstance> transferInstanceList;
    private static final String REVIEWED_INSTANCE_LIST_SORTING_ORDER = "reviewedInstanceListSortingOrder";
    public static final String MODE = "mode";


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        setHasOptionsMenu(true);
        View view = inflater.inflate(R.layout.fragment_instances, container, false);
        ButterKnife.bind(this, view);

        instanceMap = new HashMap<>();
        transferInstanceList = new ArrayList<>();
        selectedInstances = new LinkedHashSet<>();

        LinearLayoutManager llm = new LinearLayoutManager(getActivity());
        llm.setOrientation(LinearLayoutManager.VERTICAL);
        recyclerView.setLayoutManager(llm);

        toggleButton.setText(getString(R.string.select_all));
        sendButton.setText(getString(R.string.send_forms));

        setupAdapter();
        return view;
    }

    @Override
    protected void updateAdapter() {
        getInstanceFromDB();
        setEmptyViewVisibility(getString(R.string.no_forms_reviewed,
                getActivity().getIntent().getStringExtra(FORM_DISPLAY_NAME)));
        transferInstanceAdapter.notifyDataSetChanged();
    }

    @Override
    protected String getSortingOrderKey() {
        return REVIEWED_INSTANCE_LIST_SORTING_ORDER;
    }

    @Override
    public void onResume() {
        getInstanceFromDB();

        setEmptyViewVisibility(getString(R.string.no_forms_reviewed,
                getActivity().getIntent().getStringExtra(FORM_DISPLAY_NAME)));
        transferInstanceAdapter.notifyDataSetChanged();
        super.onResume();
    }

    private void getInstanceFromDB() {
        transferInstanceList.clear();
        selectedInstances.clear();
        String formVersion = getActivity().getIntent().getStringExtra(FORM_VERSION);
        String formId = getActivity().getIntent().getStringExtra(FORM_ID);
        String []selectionArgs;
        String selection;

        if (formVersion == null) {
            selection = InstanceProviderAPI.InstanceColumns.JR_FORM_ID + "=? AND "
                    + InstanceProviderAPI.InstanceColumns.JR_VERSION + " IS NULL";
            if (getFilterText().length() == 0) {
                selectionArgs = new String[]{formId};
            } else {
                selectionArgs = new String[] {formId,  "%" + getFilterText() + "%"};
                selection = "AND " + InstanceProviderAPI.InstanceColumns.DISPLAY_NAME + " LIKE ?";
            }
        } else {
            selection = InstanceProviderAPI.InstanceColumns.JR_FORM_ID + "=? AND "
                    + InstanceProviderAPI.InstanceColumns.JR_VERSION + "=?";
            if (getFilterText().length() == 0) {
                selectionArgs = new String[]{formId, formVersion};
            } else {
                selectionArgs = new String[] {formId,  "%" + getFilterText() + "%"};
                selection = "AND " + InstanceProviderAPI.InstanceColumns.DISPLAY_NAME + " LIKE ?";
            }
        }

        Cursor cursor = new InstancesDao().getInstancesCursor(null, selection, selectionArgs, getSortingOrder());
        instanceMap = new InstancesDao().getMapFromCursor(cursor);

        Cursor transferCursor = new TransferDao().getReviewedInstancesCursor();
        List<TransferInstance> transferInstances = new TransferDao().getInstancesFromCursor(transferCursor);
        for (TransferInstance instance : transferInstances) {
            if (instanceMap.containsKey(instance.getInstanceId())) {
                instance.setInstance(instanceMap.get(instance.getInstanceId()));
                transferInstanceList.add(instance);
            }
        }
    }

    private void setupAdapter() {
        transferInstanceAdapter = new TransferInstanceAdapter(getActivity(), transferInstanceList, this, selectedInstances, true);
        recyclerView.setAdapter(transferInstanceAdapter);
    }

    private void setEmptyViewVisibility(String text) {
        if (transferInstanceList.size() > 0) {
            buttonLayout.setVisibility(View.VISIBLE);
            emptyView.setVisibility(View.GONE);
        } else {
            buttonLayout.setVisibility(View.GONE);
            emptyView.setVisibility(View.VISIBLE);
            emptyView.setText(text);
        }
    }

    @Override
    public void onItemClick(View view, int position) {
        CheckBox checkBox = view.findViewById(R.id.checkbox);
        checkBox.setChecked(!checkBox.isChecked());

        TransferInstance transferInstance = transferInstanceList.get(position);
        Long id = transferInstance.getId();

        if (selectedInstances.contains(id)) {
            selectedInstances.remove(id);
        } else {
            selectedInstances.add(id);
        }
        toggleButtonLabel();
    }

    @OnClick(R.id.bToggle)
    public void toggle() {
        boolean newState = transferInstanceAdapter.getItemCount() > selectedInstances.size();

        if (newState) {
            for (TransferInstance instance: transferInstanceList) {
                selectedInstances.add(instance.getId());
            }
        } else {
            selectedInstances.clear();
        }

        transferInstanceAdapter.notifyDataSetChanged();
        toggleButtonLabel();
    }

    private void toggleButtonLabel() {
        toggleButton.setText(selectedInstances.size() == transferInstanceAdapter.getItemCount() ?
                getString(R.string.clear_all) : getString(R.string.select_all));
        sendButton.setEnabled(selectedInstances.size() > 0);
    }

    @OnClick(R.id.bAction)
    public void sendForms() {
        List<Long> instanceIds = new ArrayList<>();
        for (TransferInstance transferInstance: transferInstanceList) {
            if (selectedInstances.contains(transferInstance.getId())) {
                instanceIds.add(transferInstance.getInstanceId());
            }
        }
        Intent intent = new Intent(getContext(), SendActivity.class);
        Long[] arr = instanceIds.toArray(new Long[instanceIds.size()]);
        long[] a = ArrayUtils.toPrimitive(arr);
        intent.putExtra(INSTANCE_IDS, a);
        intent.putExtra(MODE, ApplicationConstants.SEND_REVIEW_MODE);
        startActivity(intent);
    }
}
