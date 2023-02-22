package org.odk.share.views.ui.instance.fragment;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.odk.collect.android.dao.InstancesDao;
import org.odk.collect.android.dto.Instance;
import org.odk.collect.android.provider.InstanceProviderAPI;
import org.odk.share.R;
import org.odk.share.utilities.ApplicationConstants;
import org.odk.share.utilities.ArrayUtils;
import org.odk.share.utilities.DialogUtils;
import org.odk.share.views.ui.common.InstanceListFragment;
import org.odk.share.views.ui.review.ReviewFormActivity;
import org.odk.share.views.ui.instance.adapter.TransferInstanceAdapter;
import org.odk.share.dao.TransferDao;
import org.odk.share.dto.TransferInstance;
import org.odk.share.views.listeners.OnItemClickListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;

import javax.inject.Inject;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

import static org.odk.share.views.ui.instance.InstancesList.INSTANCE_IDS;
import static org.odk.share.views.ui.main.MainActivity.FORM_DISPLAY_NAME;
import static org.odk.share.views.ui.main.MainActivity.FORM_ID;
import static org.odk.share.views.ui.main.MainActivity.FORM_VERSION;
import static org.odk.share.views.ui.review.ReviewFormActivity.INSTANCE_ID;
import static org.odk.share.views.ui.review.ReviewFormActivity.TRANSFER_ID;

/**
 * Created by laksh on 6/27/2018.
 */

public class ReceivedInstancesFragment extends InstanceListFragment implements OnItemClickListener {

    private static final String RECEIVED_INSTANCE_LIST_SORTING_ORDER = "receivedInstanceListSortingOrder";
    private static final String REVIEWED_INSTANCE_LIST_SORTING_ORDER = "reviewedInstanceListSortingOrder";
    public static final String MODE = "mode";
    public static final int MODE_REVIEW = 1;
    public static final int MODE_RECEIVE = 2;


    @BindView(R.id.recyclerview)
    RecyclerView recyclerView;
    @BindView(R.id.buttonholder)
    LinearLayout buttonLayout;
    @BindView(R.id.empty_view)
    TextView emptyView;
    @BindView(R.id.bToggle)
    Button toggleButton;
    @BindView(R.id.bAction)
    Button sendButton;
    @BindView(R.id.bToggleWide)
    Button selectReviewedButton;

    @Inject
    InstancesDao instancesDao;

    @Inject
    TransferDao transferDao;

    HashMap<Long, Instance> instanceMap;
    TransferInstanceAdapter transferInstanceAdapter;
    List<TransferInstance> transferInstanceList;
    private int activityMode;
    private String sortingOrder;

    boolean showCheckBox = false;

    public ReceivedInstancesFragment() {
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        setHasOptionsMenu(true);
        View view = inflater.inflate(R.layout.fragment_instances, container, false);
        ButterKnife.bind(this, view);

        activityMode = MODE_RECEIVE;
        sortingOrder = RECEIVED_INSTANCE_LIST_SORTING_ORDER;

        instanceMap = new HashMap<>();
        transferInstanceList = new ArrayList<>();
        selectedInstances = new LinkedHashSet<>();

        buttonLayout.setVisibility(View.GONE);
        selectReviewedButton.setVisibility(View.VISIBLE);

        LinearLayoutManager llm = new LinearLayoutManager(getActivity());
        llm.setOrientation(RecyclerView.VERTICAL);
        recyclerView.setLayoutManager(llm);
        setupAdapter();
        return view;
    }

    @Override
    public void onResume() {
        getReceivedInstanceFromDB(activityMode);
        setEmptyViewVisibility(getString(R.string.no_forms_received,
                getActivity().getIntent().getStringExtra(FORM_DISPLAY_NAME)));
        transferInstanceAdapter.notifyDataSetChanged();
        super.onResume();
    }


    private void getReceivedInstanceFromDB(int mode) {
        transferInstanceList.clear();
        selectedInstances.clear();
        String formVersion = getActivity().getIntent().getStringExtra(FORM_VERSION);
        String formId = getActivity().getIntent().getStringExtra(FORM_ID);
        String[] selectionArgs;
        String selection;

        if (formVersion == null) {
            selection = InstanceProviderAPI.InstanceColumns.JR_FORM_ID + "=? AND "
                    + InstanceProviderAPI.InstanceColumns.JR_VERSION + " IS NULL";
            if (getFilterText().length() == 0) {
                selectionArgs = new String[]{formId};
            } else {
                selectionArgs = new String[]{formId, "%" + getFilterText() + "%"};
                selection = "AND " + InstanceProviderAPI.InstanceColumns.DISPLAY_NAME + " LIKE ?";
            }
        } else {
            selection = InstanceProviderAPI.InstanceColumns.JR_FORM_ID + "=? AND "
                    + InstanceProviderAPI.InstanceColumns.JR_VERSION + "=?";
            if (getFilterText().length() == 0) {
                selectionArgs = new String[]{formId, formVersion};
            } else {
                selectionArgs = new String[]{formId, "%" + getFilterText() + "%"};
                selection = "AND " + InstanceProviderAPI.InstanceColumns.DISPLAY_NAME + " LIKE ?";
            }
        }

        Cursor cursor = instancesDao.getInstancesCursor(null, selection, selectionArgs, getSortingOrder());
        instanceMap = instancesDao.getMapFromCursor(cursor);
        Cursor transferCursor;
        if (mode == MODE_RECEIVE) {
            transferCursor = transferDao.getReceiveInstancesCursor();
        } else if (mode == MODE_REVIEW) {
            transferCursor = transferDao.getReviewedInstancesCursor();
        } else {
            transferCursor = transferDao.getReceiveInstancesCursor();
        }
        List<TransferInstance> transferInstances = transferDao.getInstancesFromCursor(transferCursor);
        for (TransferInstance instance : transferInstances) {
            if (instanceMap.containsKey(instance.getInstanceId())) {
                instance.setInstance(instanceMap.get(instance.getInstanceId()));
                transferInstanceList.add(instance);
            }
        }
        if (transferInstanceList.isEmpty()){
            buttonLayout.setVisibility(View.GONE);
            selectReviewedButton.setVisibility(View.GONE);
        }
    }

    private void setupAdapter() {
        transferInstanceAdapter = new TransferInstanceAdapter(getActivity(), transferInstanceList,
                this, selectedInstances, showCheckBox);
        recyclerView.setAdapter(transferInstanceAdapter);
    }

    private void setEmptyViewVisibility(String text) {
        if (transferInstanceList.size() > 0) {
            recyclerView.setVisibility(View.VISIBLE);
            emptyView.setVisibility(View.GONE);
        } else {
            recyclerView.setVisibility(View.GONE);
            emptyView.setVisibility(View.VISIBLE);
            emptyView.setText(text);
        }
    }

    @Override
    public void onItemClick(View view, int position) {

        if (activityMode == MODE_RECEIVE) {
            Intent intent = new Intent(getContext(), ReviewFormActivity.class);
            intent.putExtra(INSTANCE_ID, transferInstanceList.get(position).getInstanceId());
            intent.putExtra(TRANSFER_ID, transferInstanceList.get(position).getId());
            startActivity(intent);
        } else if (activityMode == MODE_REVIEW) {

            TransferInstance transferInstance = transferInstanceList.get(position);
            Long id = transferInstance.getId();

            if (selectedInstances.contains(id)) {
                selectedInstances.remove(id);

                view.setBackgroundResource(R.color.colorTabActive);
            } else {
                selectedInstances.add(id);
                view.setBackgroundResource(R.color.colorSelected);
            }
            toggleButtonLabel();
        }
    }

    @Override
    protected void updateAdapter() {
        getReceivedInstanceFromDB(activityMode);
        setEmptyViewVisibility(getString(R.string.no_forms_received,
                getActivity().getIntent().getStringExtra(FORM_DISPLAY_NAME)));
        transferInstanceAdapter.notifyDataSetChanged();
    }

    @Override
    protected String getSortingOrderKey() {
        return sortingOrder;
    }

    private void toggleButtonLabel() {
        toggleButton.setText(selectedInstances.size() == transferInstanceAdapter.getItemCount() ?
                getString(R.string.clear_all) : getString(R.string.select_all));
        sendButton.setEnabled(selectedInstances.size() > 0);
    }


    @OnClick(R.id.bToggleWide)
    public void changeMode() {
        activityMode = MODE_REVIEW;
        sortingOrder = REVIEWED_INSTANCE_LIST_SORTING_ORDER;


        selectReviewedButton.setVisibility(View.GONE);
        buttonLayout.setVisibility(View.VISIBLE);
        toggleButton.setText(getString(R.string.select_all));
        sendButton.setText(getString(R.string.send_forms));


        getReceivedInstanceFromDB(activityMode);
        setEmptyViewVisibility(getString(R.string.no_forms_received,
                getActivity().getIntent().getStringExtra(FORM_DISPLAY_NAME)));
        transferInstanceAdapter.notifyDataSetChanged();

    }

    @OnClick(R.id.bToggle)
    public void toggle() {
        boolean newState = transferInstanceAdapter.getItemCount() > selectedInstances.size();

        if (newState) {
            for (TransferInstance instance : transferInstanceList) {
                selectedInstances.add(instance.getId());
            }
        } else {
            selectedInstances.clear();
        }

        transferInstanceAdapter.notifyDataSetChanged();
        toggleButtonLabel();
    }

    @OnClick(R.id.bAction)
    public void sendForms() {
        List<Long> instanceIds = new ArrayList<>();
        for (TransferInstance transferInstance : transferInstanceList) {
            if (selectedInstances.contains(transferInstance.getId())) {
                instanceIds.add(transferInstance.getInstanceId());
            }
        }

        Intent intent = new Intent();
        Long[] arr = instanceIds.toArray(new Long[instanceIds.size()]);
        long[] a = ArrayUtils.toPrimitive(arr);
        intent.putExtra(INSTANCE_IDS, a);
        intent.putExtra(MODE, ApplicationConstants.SEND_REVIEW_MODE);

        if (getContext() != null) {
            DialogUtils.switchToDefaultSendingMethod(getContext(), intent);
        }
    }
}
