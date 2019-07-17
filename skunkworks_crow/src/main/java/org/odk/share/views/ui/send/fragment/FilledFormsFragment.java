package org.odk.share.views.ui.send.fragment;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.loader.app.LoaderManager;
import androidx.loader.content.Loader;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.odk.collect.android.dao.InstancesDao;
import org.odk.collect.android.provider.InstanceProviderAPI;
import org.odk.share.R;
import org.odk.share.utilities.ApplicationConstants;
import org.odk.share.utilities.ArrayUtils;
import org.odk.share.utilities.DialogUtils;
import org.odk.share.views.ui.common.InstanceListFragment;
import org.odk.share.views.ui.instance.adapter.InstanceAdapter;

import java.util.LinkedHashSet;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

import static org.odk.share.views.ui.instance.fragment.ReviewedInstancesFragment.MODE;

/**
 * Created by laksh on 10/29/2018.
 */

public class FilledFormsFragment extends InstanceListFragment implements LoaderManager.LoaderCallbacks<Cursor> {

    public static final String INSTANCE_IDS = "instance_ids";
    private static final String INSTANCE_LIST_ACTIVITY_SORTING_ORDER = "instanceListActivitySortingOrder";
    private static final int INSTANCE_LOADER = 1;

    @BindView(R.id.recyclerview)
    RecyclerView recyclerView;
    @BindView(R.id.buttonholder)
    LinearLayout buttonLayout;
    @BindView(R.id.empty_view)
    TextView emptyView;
    @BindView(R.id.send_button)
    Button sendButton;
    @BindView(R.id.toggle_button)
    Button toggleButton;

    @Inject
    InstancesDao instancesDao;

    private InstanceAdapter instanceAdapter;
    private LinkedHashSet<Long> selectedInstances;

    public FilledFormsFragment() {
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        setHasOptionsMenu(true);
        View view = inflater.inflate(R.layout.fragment_forms, container, false);
        ButterKnife.bind(this, view);

        selectedInstances = new LinkedHashSet<>();

        LinearLayoutManager llm = new LinearLayoutManager(getActivity());
        llm.setOrientation(RecyclerView.VERTICAL);
        recyclerView.setLayoutManager(llm);
        return view;
    }

    @Override
    public void onResume() {
        getActivity().getSupportLoaderManager().initLoader(INSTANCE_LOADER, null, this);
        super.onResume();
    }

    @NonNull
    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return instancesDao.getSavedInstancesCursorLoader(getFilterText(), getSortingOrder());
    }

    @Override
    public void onLoadFinished(@NonNull Loader<Cursor> loader, Cursor cursor) {
        if (cursor != null) {
            cursor.moveToFirst();
            instanceAdapter = new InstanceAdapter(getActivity(), cursor, this::onListItemClick, selectedInstances);
            recyclerView.setAdapter(instanceAdapter);
            setEmptyViewVisibility(cursor.getCount());
            if (instanceAdapter.getItemCount() > 0) {
                toggleButton.setText(getString(R.string.select_all));
                toggleButton.setEnabled(true);
            } else {
                toggleButton.setEnabled(false);
            }
        } else {
            setEmptyViewVisibility(0);
        }
        addListItemDivider();
    }


    @Override
    public void onLoaderReset(@NonNull Loader loader) {
    }

    private void onListItemClick(View view, int position) {
        Cursor cursor = instanceAdapter.getCursor();
        cursor.moveToPosition(position);

        CheckBox checkBox = view.findViewById(R.id.checkbox);
        checkBox.setChecked(!checkBox.isChecked());

        long id = cursor.getLong(cursor.getColumnIndex(InstanceProviderAPI.InstanceColumns._ID));

        if (selectedInstances.contains(id)) {
            selectedInstances.remove(id);
        } else {
            selectedInstances.add(id);
        }

        sendButton.setEnabled(selectedInstances.size() > 0);

        toggleButtonLabel();
    }

    private void setEmptyViewVisibility(int len) {
        if (len > 0) {
            recyclerView.setVisibility(View.VISIBLE);
            emptyView.setVisibility(View.GONE);
        } else {
            recyclerView.setVisibility(View.GONE);
            emptyView.setVisibility(View.VISIBLE);
            emptyView.setText(getString(R.string.no_forms));
        }
    }

    @OnClick(R.id.send_button)
    public void send() {
        if (getContext() != null) {
            Intent intent = new Intent();
            setupSendingIntent(intent);
            DialogUtils.showSenderMethodsDialog(getContext(), intent, getString(R.string.title_send_options)).show();
        }
    }

    private void setupSendingIntent(Intent intent) {
        Long[] arr = selectedInstances.toArray(new Long[selectedInstances.size()]);
        long[] a = ArrayUtils.toPrimitive(arr);
        intent.putExtra(INSTANCE_IDS, a);
        intent.putExtra(MODE, ApplicationConstants.ASK_REVIEW_MODE);
    }

    @OnClick(R.id.toggle_button)
    public void toggle() {
        boolean newState = instanceAdapter.getItemCount() > selectedInstances.size();
        sendButton.setEnabled(newState);

        if (newState) {
            Cursor cursor = instanceAdapter.getCursor();
            if (cursor.moveToFirst()) {
                do {
                    selectedInstances.add(cursor.getLong(cursor.getColumnIndex(InstanceProviderAPI.InstanceColumns._ID)));
                } while (cursor.moveToNext());
            }
        } else {
            selectedInstances.clear();
        }

        instanceAdapter.notifyDataSetChanged();
        toggleButtonLabel();
    }

    private void toggleButtonLabel() {
        if (selectedInstances.size() == instanceAdapter.getItemCount()) {
            toggleButton.setText(getString(R.string.clear_all));
        } else {
            toggleButton.setText(getString(R.string.select_all));
        }
        if (selectedInstances.isEmpty()) {
            sendButton.setText(getString(R.string.send_forms));
        } else {
            sendButton.setText(String.format(getString(R.string.send_count), selectedInstances.size()));
        }
    }

    @Override
    protected void updateAdapter() {
        getActivity().getSupportLoaderManager().restartLoader(INSTANCE_LOADER, null, this);
    }

    @Override
    protected String getSortingOrderKey() {
        return INSTANCE_LIST_ACTIVITY_SORTING_ORDER;
    }

    private void addListItemDivider() {
        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(
                recyclerView.getContext(),
                DividerItemDecoration.VERTICAL);

        recyclerView.addItemDecoration(dividerItemDecoration);
    }
}
