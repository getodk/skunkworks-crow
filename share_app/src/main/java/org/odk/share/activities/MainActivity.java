package org.odk.share.activities;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import org.odk.share.R;
import org.odk.share.adapters.FormsAdapter;
import org.odk.share.application.Share;
import org.odk.share.dao.FormsDao;
import org.odk.share.dao.InstancesDao;
import org.odk.share.dao.TransferDao;
import org.odk.share.dto.TransferInstance;
import org.odk.share.preferences.SettingsPreference;
import org.odk.share.provider.FormsProviderAPI;
import org.odk.share.provider.InstanceProviderAPI;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import timber.log.Timber;

public class MainActivity extends FormListActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    public static final String FORM_VERSION = "form_version";
    public static final String FORM_ID = "form_id";
    public static final String FORM_DISPLAY_NAME = "form_display_name";
    private static final String FORM_CHOOSER_LIST_SORTING_ORDER = "formChooserListSortingOrder";
    private static final String COLLECT_PACKAGE = "org.odk.collect.android";
    public static final String REVIEWED = "reviewed";
    public static final String UNREVIEWED = "unreviewed";

    private static final int FORM_LOADER = 2;

    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.bSendForms)
    Button sendForms;
    @BindView(R.id.bViewWifi)
    Button viewWifi;
    @BindView(R.id.recyclerview)
    RecyclerView recyclerView;
    @BindView(R.id.empty_view)
    TextView emptyView;

    private FormsAdapter formAdapter;
    Map<String, Map<String, Map<String, Integer>>> formMap;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        setTitle(getString(R.string.app_name));
        setSupportActionBar(toolbar);
        Share.createODKDirs();
        formMap = new HashMap<>();

        LinearLayoutManager llm = new LinearLayoutManager(this);
        llm.setOrientation(LinearLayoutManager.VERTICAL);
        recyclerView.setLayoutManager(llm);
        setupAdapter();
        getSupportLoaderManager().initLoader(FORM_LOADER, null, this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (isCollectInstalled()) {
            setupAdapter();
            getSupportLoaderManager().initLoader(FORM_LOADER, null, this);
        } else {
            showAlertDialog();
        }
    }

    private void setupAdapter() {
        formAdapter = new FormsAdapter(this, null, this::onItemClick, formMap);
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
            case R.id.menu_about:
                startActivity(new Intent(this, AboutActivity.class));
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void updateAdapter() {
        getSupportLoaderManager().restartLoader(FORM_LOADER, null, this);
    }

    @Override
    protected String getSortingOrderKey() {
        return FORM_CHOOSER_LIST_SORTING_ORDER;
    }

    @NonNull
    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return new FormsDao().getFormsCursorLoader(getFilterText(), getSortingOrder());
    }

    @Override
    public void onLoadFinished(@NonNull Loader<Cursor> loader, Cursor cursor) {
        formAdapter.changeCursor(cursor);
        if (cursor != null && !cursor.isClosed()) {
            setEmptyViewVisibility(cursor.getCount());
        }
    }

    @Override
    public void onLoaderReset(@NonNull Loader loader) {
        formAdapter.swapCursor(null);
    }

    private void setEmptyViewVisibility(int len) {
        if (len > 0) {
            recyclerView.setVisibility(View.VISIBLE);
            emptyView.setVisibility(View.GONE);
        } else {
            recyclerView.setVisibility(View.GONE);
            emptyView.setVisibility(View.VISIBLE);
            emptyView.setText(getString(R.string.no_blank_forms));
        }
    }

    private void onItemClick(View view, int position) {

        Intent intent  = new Intent(this, InstanceManagerTabs.class);

        try (Cursor cursor = formAdapter.getCursor()) {
            if (cursor != null) {
                cursor.moveToPosition(position);
                intent.putExtra(FORM_VERSION, cursor.getString(cursor.getColumnIndex(FormsProviderAPI.FormsColumns.JR_VERSION)));
                intent.putExtra(FORM_ID, cursor.getString(cursor.getColumnIndex(FormsProviderAPI.FormsColumns.JR_FORM_ID)));
                intent.putExtra(FORM_DISPLAY_NAME, cursor.getString(cursor.getColumnIndex(FormsProviderAPI.FormsColumns.DISPLAY_NAME)));
            }
        }
        startActivity(intent);
    }

    private boolean isCollectInstalled() {
        PackageManager packageManager = getPackageManager();
        try {
            packageManager.getPackageInfo(COLLECT_PACKAGE, PackageManager.GET_ACTIVITIES);
            return true;
        } catch (PackageManager.NameNotFoundException e) {
            Timber.d("Collect not installed");
        }
        return false;
    }

    private void showAlertDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.install_collect);
        builder.setPositiveButton(getString(R.string.install), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                try {
                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + COLLECT_PACKAGE)));
                } catch (android.content.ActivityNotFoundException e) {
                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=" + COLLECT_PACKAGE)));
                }
            }
        });
        builder.setNegativeButton(getString(R.string.cancel), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                finish();
            }
        });

        builder.setCancelable(false);
        builder.show();
    }

    private void getFormStatus() {
        try (Cursor transferCursor = new TransferDao().getReceiveInstancesCursor()) {
            List<TransferInstance> transferInstanceList = new TransferDao().getInstancesFromCursor(transferCursor);
            int len = transferCursor.getCount();
            HashMap<Long, TransferInstance> idMap = new HashMap<>();
            StringBuilder selectionBuf = new StringBuilder(InstanceProviderAPI.InstanceColumns._ID + " IN (");
            String[] selectionArgs = new String[len];
            for (int i = 0; i < transferInstanceList.size(); i++) {
                TransferInstance instance = transferInstanceList.get(i);
                idMap.put(instance.getInstanceId(), instance);
                if (i > 0) {
                    selectionBuf.append(",");
                }
                selectionBuf.append("?");
                selectionArgs[i] = String.valueOf(transferInstanceList.get(i).getInstanceId());
            }

            selectionBuf.append(")");
            String selection = selectionBuf.toString();

            try (Cursor instanceCursor = new InstancesDao().getInstancesCursor(selection, selectionArgs)) {
                if (instanceCursor != null && instanceCursor.getCount() > 0) {
                    instanceCursor.moveToPosition(-1);
                    while (instanceCursor.moveToNext()) {
                        String formId = instanceCursor.getString(instanceCursor.getColumnIndex(InstanceProviderAPI.InstanceColumns.JR_FORM_ID));
                        String formVersion = instanceCursor.getString(instanceCursor.getColumnIndex(InstanceProviderAPI.InstanceColumns.JR_VERSION));

                        Map<String, Map<String, Integer>> instanceMap;
                        if (formMap.containsKey(formId)) {
                            instanceMap = formMap.get(formId);
                        } else {
                            instanceMap = new HashMap<>();
                            formMap.put(formId, instanceMap);
                        }

                        Map<String, Integer> statusMap;
                        if (instanceMap.containsKey(formVersion)) {
                            statusMap = instanceMap.get(formVersion);
                        } else {
                            statusMap = new HashMap<>();
                            instanceMap.put(formVersion, statusMap);
                        }

                        TransferInstance transferInstance = idMap.get(instanceCursor.getLong(instanceCursor.getColumnIndex(InstanceProviderAPI.InstanceColumns._ID)));
                        if (transferInstance.getReviewed() == TransferInstance.STATUS_REJECTED || transferInstance.getReviewed() == TransferInstance.STATUS_ACCEPTED) {
                            // Reviewed
                            if (statusMap.containsKey(REVIEWED)) {
                                statusMap.put(REVIEWED, statusMap.get(REVIEWED) + 1);
                            } else {
                                statusMap.put(REVIEWED, 1);
                            }
                        } else {
                            // Unreviewed
                            if (statusMap.containsKey(UNREVIEWED)) {
                                statusMap.put(UNREVIEWED, statusMap.get(UNREVIEWED) + 1);
                            } else {
                                statusMap.put(UNREVIEWED, 1);
                            }
                        }
                    }
                }
                Timber.d(String.valueOf(formMap));
            }
        }
    }
}