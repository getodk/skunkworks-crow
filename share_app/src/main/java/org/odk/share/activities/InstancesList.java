package org.odk.share.activities;

import android.content.Intent;
import android.database.Cursor;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import org.odk.share.R;
import org.odk.share.adapters.InstanceAdapter;
import org.odk.share.dao.InstancesDao;
import org.odk.share.provider.InstanceProviderAPI;
import org.odk.share.utilities.ArrayUtils;


import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;

import java.util.LinkedHashSet;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class InstancesList extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    @BindView(R.id.recyclerview) RecyclerView recyclerView;
    @BindView(R.id.toolbar) Toolbar toolbar;
    @BindView(R.id.send_button) Button sendButton;
    @BindView(R.id.toggle_button) Button toggleButton;

    protected static final String SORT_BY_NAME_ASC
            = InstanceProviderAPI.InstanceColumns.DISPLAY_NAME + " COLLATE NOCASE ASC";

    private static final String SELECTED_INSTANCES = "selectedInstances";

    private static final int INSTANCE_LOADER = 1;
    private InstanceAdapter instanceAdapter;
    private LinkedHashSet<Long> selectedInstances;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_instances_list);
        ButterKnife.bind(this);

        setTitle(getString(R.string.saved_forms));
        setSupportActionBar(toolbar);

        selectedInstances = new LinkedHashSet<>();

        LinearLayoutManager llm = new LinearLayoutManager(this);
        llm.setOrientation(LinearLayoutManager.VERTICAL);
        recyclerView.setLayoutManager(llm);
    }

    @Override
    protected void onResume() {
        getSupportLoaderManager().initLoader(INSTANCE_LOADER, null, this);
        super.onResume();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putSerializable(SELECTED_INSTANCES, selectedInstances);
    }

    @Override
    protected void onRestoreInstanceState(Bundle state) {
        super.onRestoreInstanceState(state);
        selectedInstances = (LinkedHashSet<Long>) state.getSerializable(SELECTED_INSTANCES);
    }

    @NonNull
    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return new InstancesDao().getSavedInstancesCursorLoader(SORT_BY_NAME_ASC);
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
        Intent intent = new Intent(this, SendActivity.class);
        Long[] arr = selectedInstances.toArray(new Long[selectedInstances.size()]);
        long[] a = ArrayUtils.toPrimitive(arr);
        intent.putExtra("instance_ids", a);
        startActivity(intent);
        finish();
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
}
