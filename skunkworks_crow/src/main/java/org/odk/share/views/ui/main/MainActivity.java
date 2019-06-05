package org.odk.share.views.ui.main;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import org.odk.collect.android.dao.FormsDao;
import org.odk.collect.android.dao.InstancesDao;
import org.odk.collect.android.dto.Form;
import org.odk.share.R;
import org.odk.share.views.ui.common.basecursor.BaseCursorViewHolder;
import org.odk.share.views.listeners.ItemClickListener;
import org.odk.share.application.Share;
import org.odk.share.dao.TransferDao;
import org.odk.share.views.ui.settings.SettingsActivity;
import org.odk.share.views.ui.about.AboutActivity;
import org.odk.share.views.ui.instance.InstanceManagerTabs;
import org.odk.share.views.ui.send.SendFormsActivity;
import org.odk.share.views.ui.receive.WifiActivity;

import javax.inject.Inject;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.Toolbar;
import androidx.loader.app.LoaderManager;
import androidx.loader.content.Loader;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
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
    private static final int STORAGE_PERMISSION_REQUEST_CODE = 101;
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

    @Inject
    InstancesDao instancesDao;

    @Inject
    FormsDao formsDao;

    @Inject
    TransferDao transferDao;

    private FormsAdapter formAdapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        setTitle(getString(R.string.app_name));
        setSupportActionBar(toolbar);
        createODKDirs();

        sendForms.setEnabled(false);

        LinearLayoutManager llm = new LinearLayoutManager(this);
        llm.setOrientation(RecyclerView.VERTICAL);
        recyclerView.setLayoutManager(llm);

        //check the storage permission and start the loader
        setUpLoader();

        addListItemDivider();
    }

    private void setupAdapter() {
        formAdapter = new FormsAdapter(this, null, this, instancesDao, transferDao);
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
                startActivity(new Intent(this, SettingsActivity.class));
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
        return formsDao.getFormsCursorLoader(getFilterText(), getSortingOrder());
    }

    @Override
    public void onLoadFinished(@NonNull Loader<Cursor> loader, Cursor cursor) {
        formAdapter.changeCursor(cursor);
        if (cursor != null && !cursor.isClosed()) {
            setEmptyViewVisibility(cursor.getCount());
            return;
        }
        setEmptyViewVisibility(0);
    }

    @Override
    public void onLoaderReset(@NonNull Loader loader) {
        formAdapter.swapCursor(null);
    }

    private void setEmptyViewVisibility(int len) {
        if (len > 0) {
            recyclerView.setVisibility(View.VISIBLE);
            emptyView.setVisibility(View.GONE);
            sendForms.setEnabled(true);
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

    // call createODKDirs() with a permission check.
    private void createODKDirs() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M ||
                checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
            Share.createODKDirs(this);
        } else {
            requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE},
                    STORAGE_PERMISSION_REQUEST_CODE);
        }
    }

    private void addListItemDivider() {
        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(
                recyclerView.getContext(),
                DividerItemDecoration.VERTICAL);

        recyclerView.addItemDecoration(dividerItemDecoration);

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        switch (requestCode) {
            case STORAGE_PERMISSION_REQUEST_CODE:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    setUpLoader();
                } else {
                    //close the app if the permission is denied
                    finish();
                }
        }
    }

    private void setUpLoader() {

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M ||
                checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {

            setupAdapter();
            getSupportLoaderManager().initLoader(FORM_LOADER, null, this);

            if (isCollectInstalled()) {
                updateAdapter();
            } else {
                showAlertDialog();
            }
        }
    }
}