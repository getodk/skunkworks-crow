package org.odk.share.activities;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

import org.odk.share.R;
import org.odk.share.adapters.FormsAdapter;
import org.odk.share.application.Share;
import org.odk.share.dao.FormsDao;
import org.odk.share.preferences.SettingsPreference;
import org.odk.share.provider.FormsProviderAPI;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class MainActivity extends InjectableActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    protected static final String SORT_BY_NAME_ASC
            = FormsProviderAPI.FormsColumns.DISPLAY_NAME + " COLLATE NOCASE ASC";
    public static final String FORM_VERSION = "form_version";
    public static final String FORM_ID = "form_id";
    public static final String FORM_DISPLAY_NAME = "form_display_name";

    private static final int FORM_LOADER = 2;

    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.bSendForms)
    Button sendForms;
    @BindView(R.id.bViewWifi)
    Button viewWifi;
    @BindView(R.id.recyclerview)
    RecyclerView recyclerView;

    private FormsAdapter formAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        setTitle(getString(R.string.app_name));
        setSupportActionBar(toolbar);

        Share.createODKDirs();

        LinearLayoutManager llm = new LinearLayoutManager(this);
        llm.setOrientation(LinearLayoutManager.VERTICAL);
        recyclerView.setLayoutManager(llm);
    }

    @Override
    protected void onResume() {
        super.onResume();
        setupAdapter();
        getSupportLoaderManager().initLoader(FORM_LOADER, null, this);
    }

    private void setupAdapter() {
        formAdapter = new FormsAdapter(this, null, this::onItemClick);
        recyclerView.setAdapter(formAdapter);
    }

    @OnClick(R.id.bViewWifi)
    public void viewWifiNetworks() {
        Intent intent = new Intent(this, WifiActivity.class);
        startActivity(intent);
    }

    @OnClick(R.id.bSendForms)
    public void selectForms() {
        Intent intent = new Intent(this, InstancesList.class);
        startActivity(intent);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_settings:
                startActivity(new Intent(this, SettingsPreference.class));
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @NonNull
    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return new FormsDao().getFormsCursorLoader(SORT_BY_NAME_ASC);
    }

    @Override
    public void onLoadFinished(@NonNull Loader<Cursor> loader, Cursor cursor) {
        formAdapter.changeCursor(cursor);
    }

    @Override
    public void onLoaderReset(@NonNull Loader loader) {
        formAdapter.swapCursor(null);
    }

    private void onItemClick(View view, int position) {

        Intent intent  = new Intent(this, InstanceManagerTabs.class);
        Cursor cursor = formAdapter.getCursor();

        if (cursor != null) {
            try {
                cursor.moveToPosition(position);
                intent.putExtra(FORM_VERSION, cursor.getString(cursor.getColumnIndex(FormsProviderAPI.FormsColumns.JR_VERSION)));
                intent.putExtra(FORM_ID, cursor.getString(cursor.getColumnIndex(FormsProviderAPI.FormsColumns.JR_FORM_ID)));
                intent.putExtra(FORM_DISPLAY_NAME, cursor.getString(cursor.getColumnIndex(FormsProviderAPI.FormsColumns.DISPLAY_NAME)));
            } finally {
                cursor.close();
            }
        }
        startActivity(intent);
    }
}