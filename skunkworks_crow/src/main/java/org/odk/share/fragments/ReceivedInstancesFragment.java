package org.odk.share.fragments;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.odk.collect.android.dao.InstancesDao;
import org.odk.collect.android.dto.Instance;
import org.odk.collect.android.provider.InstanceProviderAPI;
import org.odk.share.R;
import org.odk.share.activities.ReviewFormActivity;
import org.odk.share.adapters.TransferInstanceAdapter;
import org.odk.share.dao.TransferDao;
import org.odk.share.dto.TransferInstance;
import org.odk.share.listeners.OnItemClickListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;

import static org.odk.share.activities.MainActivity.FORM_DISPLAY_NAME;
import static org.odk.share.activities.MainActivity.FORM_ID;
import static org.odk.share.activities.MainActivity.FORM_VERSION;
import static org.odk.share.activities.ReviewFormActivity.INSTANCE_ID;
import static org.odk.share.activities.ReviewFormActivity.TRANSFER_ID;

/**
 * Created by laksh on 6/27/2018.
 */

public class ReceivedInstancesFragment extends InstanceListFragment implements OnItemClickListener {

    private static final String RECEIVED_INSTANCE_LIST_SORTING_ORDER = "receivedInstanceListSortingOrder";

    @BindView(R.id.recyclerview)
    RecyclerView recyclerView;
    @BindView(R.id.buttonholder)
    LinearLayout buttonLayout;
    @BindView(R.id.empty_view)
    TextView emptyView;

    @Inject
    InstancesDao instancesDao;

    @Inject
    TransferDao transferDao;

    HashMap<Long, Instance> instanceMap;
    TransferInstanceAdapter transferInstanceAdapter;
    List<TransferInstance> transferInstanceList;

    boolean showCheckBox = false;

    public ReceivedInstancesFragment() {
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        setHasOptionsMenu(true);
        View view = inflater.inflate(R.layout.fragment_instances, container, false);
        ButterKnife.bind(this, view);

        instanceMap = new HashMap<>();
        transferInstanceList = new ArrayList<>();
        selectedInstances = new LinkedHashSet<>();

        buttonLayout.setVisibility(View.GONE);

        LinearLayoutManager llm = new LinearLayoutManager(getActivity());
        llm.setOrientation(LinearLayoutManager.VERTICAL);
        recyclerView.setLayoutManager(llm);
        setupAdapter();
        return view;
    }

    @Override
    public void onResume() {
        getInstanceFromDB();
        setEmptyViewVisibility(getString(R.string.no_forms_received,
                getActivity().getIntent().getStringExtra(FORM_DISPLAY_NAME)));
        transferInstanceAdapter.notifyDataSetChanged();
        super.onResume();
    }

    private void getInstanceFromDB() {
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
        Cursor transferCursor = transferDao.getReceiveInstancesCursor();
        List<TransferInstance> transferInstances = transferDao.getInstancesFromCursor(transferCursor);
        for (TransferInstance instance : transferInstances) {
            if (instanceMap.containsKey(instance.getInstanceId())) {
                instance.setInstance(instanceMap.get(instance.getInstanceId()));
                transferInstanceList.add(instance);
            }
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
        Intent intent = new Intent(getContext(), ReviewFormActivity.class);
        intent.putExtra(INSTANCE_ID, transferInstanceList.get(position).getInstanceId());
        intent.putExtra(TRANSFER_ID, transferInstanceList.get(position).getId());
        startActivity(intent);
    }

    @Override
    protected void updateAdapter() {
        getInstanceFromDB();
        setEmptyViewVisibility(getString(R.string.no_forms_received,
                getActivity().getIntent().getStringExtra(FORM_DISPLAY_NAME)));
        transferInstanceAdapter.notifyDataSetChanged();
    }

    @Override
    protected String getSortingOrderKey() {
        return RECEIVED_INSTANCE_LIST_SORTING_ORDER;
    }
}
