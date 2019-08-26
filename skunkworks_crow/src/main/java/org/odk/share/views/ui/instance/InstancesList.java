package org.odk.share.views.ui.instance;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.Toolbar;
import androidx.loader.app.LoaderManager;
import androidx.loader.content.Loader;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.odk.collect.android.dao.InstancesDao;
import org.odk.collect.android.provider.InstanceProviderAPI;
import org.odk.share.R;
import org.odk.share.utilities.ApplicationConstants;
import org.odk.share.utilities.ArrayUtils;
import org.odk.share.utilities.DialogUtils;
import org.odk.share.views.ui.instance.adapter.InstanceAdapter;

import java.util.LinkedHashSet;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

import static org.odk.share.views.ui.instance.fragment.ReviewedInstancesFragment.MODE;

public class InstancesList extends InstanceListActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    @BindView(R.id.recyclerview)
    RecyclerView recyclerView;
    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.send_button)
    Button sendButton;
    @BindView(R.id.toggle_button)
    Button toggleButton;

    private static final String INSTANCE_LIST_ACTIVITY_SORTING_ORDER = "instanceListActivitySortingOrder";

    public static final String INSTANCE_IDS = "instance_ids";

    private static final int INSTANCE_LOADER = 1;
    private InstanceAdapter instanceAdapter;
    private LinkedHashSet<Long> selectedInstances;

    @Inject
    InstancesDao instancesDao;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_instances_list);
        ButterKnife.bind(this);

        setTitle(getString(R.string.saved_forms));
        setSupportActionBar(toolbar);

        selectedInstances = new LinkedHashSet<>();
        LinearLayoutManager llm = new LinearLayoutManager(this);
        llm.setOrientation(RecyclerView.VERTICAL);
        recyclerView.setLayoutManager(llm);
    }

    @Override
    protected void onResume() {
        getSupportLoaderManager().initLoader(INSTANCE_LOADER, null, this);
        super.onResume();
    }

    @NonNull
    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return instancesDao.getSavedInstancesCursorLoader(getFilterText(), getSortingOrder());
    }

    @Override
    public void onLoadFinished(@NonNull Loader<Cursor> loader, Cursor cursor) {
        cursor.moveToFirst();
        instanceAdapter = new InstanceAdapter(this, cursor, this::onListItemClick, selectedInstances);
        recyclerView.setAdapter(instanceAdapter);
        if (instanceAdapter.getItemCount() > 0) {
            toggleButton.setText(getString(R.string.select_all));
            toggleButton.setEnabled(true);
        } else {
            toggleButton.setEnabled(false);
        }
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

    @OnClick(R.id.send_button)
    public void send() {
        Intent intent = new Intent();
        Long[] arr = selectedInstances.toArray(new Long[selectedInstances.size()]);
        long[] a = ArrayUtils.toPrimitive(arr);
        intent.putExtra(INSTANCE_IDS, a);
        intent.putExtra(MODE, ApplicationConstants.ASK_REVIEW_MODE);

        DialogUtils.switchToDefaultSendingMethod(this, intent);
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
    }

    @Override
    protected void updateAdapter() {
        getSupportLoaderManager().restartLoader(INSTANCE_LOADER, null, this);
    }

    @Override
    protected String getSortingOrderKey() {
        return INSTANCE_LIST_ACTIVITY_SORTING_ORDER;
    }
}
