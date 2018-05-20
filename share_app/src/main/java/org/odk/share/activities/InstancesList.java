package org.odk.share.activities;


import android.database.Cursor;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import org.odk.share.R;
import org.odk.share.adapters.InstanceAdapter;
import org.odk.share.dao.InstancesDao;
import org.odk.share.provider.InstanceProviderAPI;


import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v7.widget.Toolbar;

import butterknife.BindView;
import butterknife.ButterKnife;

public class InstancesList extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    @BindView(R.id.recyclerview) RecyclerView recyclerView;
    @BindView(R.id.toolbar) Toolbar toolbar;

    protected static final String SORT_BY_NAME_ASC
            = InstanceProviderAPI.InstanceColumns.DISPLAY_NAME + " COLLATE NOCASE ASC";

    private static final int INSTANCE_LOADER = 1;
    private InstanceAdapter instanceAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_instances_list);
        ButterKnife.bind(this);

        setTitle(getString(R.string.saved_forms));
        setSupportActionBar(toolbar);

        LinearLayoutManager llm = new LinearLayoutManager(this);
        llm.setOrientation(LinearLayoutManager.VERTICAL);
        recyclerView.setLayoutManager(llm);
        getSupportLoaderManager().initLoader(INSTANCE_LOADER, null, this);
    }


    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return new InstancesDao().getSavedInstancesCursorLoader(SORT_BY_NAME_ASC);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        cursor.moveToFirst();
        instanceAdapter = new InstanceAdapter(this, cursor);
        recyclerView.setAdapter(instanceAdapter);
    }


    @Override
    public void onLoaderReset(Loader loader) {

    }
}
