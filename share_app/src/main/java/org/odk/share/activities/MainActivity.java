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
import org.odk.share.adapters.basecursoradapter.BaseCursorViewHolder;
import org.odk.share.adapters.basecursoradapter.ItemClickListener;
import org.odk.share.application.Share;
import org.odk.share.dao.FormsDao;
import org.odk.share.dto.Form;
import org.odk.share.preferences.SettingsPreference;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import timber.log.Timber;

public class MainActivity extends FormListActivity implements LoaderManager.LoaderCallbacks<Cursor>, ItemClickListener {

    public static final String FORM_VERSION = "form_version";
    public static final String FORM_ID = "form_id";
    public static final String FORM_DISPLAY_NAME = "form_display_name";
    private static final String FORM_CHOOSER_LIST_SORTING_ORDER = "formChooserListSortingOrder";
    private static final String COLLECT_PACKAGE = "org.odk.collect.android";

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
        setupAdapter();
        getSupportLoaderManager().initLoader(FORM_LOADER, null, this);
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (isCollectInstalled()) {
            updateAdapter();
        } else {
            showAlertDialog();
        }
    }

    private void setupAdapter() {
        formAdapter = new FormsAdapter(this, null, this);
        recyclerView.setAdapter(formAdapter);
    }

    @OnClick(R.id.bViewWifi)
    public void viewWifiNetworks() {
        Intent intent = new Intent(this, WifiActivity.class);
        startActivity(intent);
    }

    @OnClick(R.id.bSendForms)
    public void selectForms() {
        Intent intent = new Intent(this, SendFormsActivity.class);
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

    @Override
    public void onItemClick(BaseCursorViewHolder holder, int position) {
        Intent intent = new Intent(this, InstanceManagerTabs.class);

        Form form = ((FormsAdapter.FormHolder) holder).getForm();

        intent.putExtra(FORM_VERSION, form.getJrVersion());
        intent.putExtra(FORM_ID, form.getJrFormId());
        intent.putExtra(FORM_DISPLAY_NAME, form.getDisplayName());

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
}