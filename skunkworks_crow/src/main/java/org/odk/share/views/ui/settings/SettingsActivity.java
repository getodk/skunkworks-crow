package org.odk.share.views.ui.settings;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.EditTextPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.CheckBox;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.Toolbar;

import com.google.android.material.textfield.TextInputLayout;

import org.odk.share.R;
import org.odk.share.application.Share;
import org.odk.share.utilities.FileUtils;


/**
 * Created by laksh on 5/27/2018.
 */

public class SettingsActivity extends PreferenceActivity {

    EditTextPreference hotspotNamePreference;
    Preference hotspotPasswordPreference;
    Preference resetPreference;
    CheckBoxPreference passwordRequirePreference;
    EditTextPreference odkDestinationDirPreference;
    private SharedPreferences prefs;
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ViewGroup root = getRootView();
        Toolbar toolbar = (Toolbar) View.inflate(this, R.layout.toolbar, null);
        toolbar.setTitle(getString(R.string.settings));
        root.addView(toolbar, 0);

        addPreferencesFromResource(R.xml.preferences_menu);
        addPreferences();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
        }
        return super.onOptionsItemSelected(item);
    }

    private void addPreferences() {
        resetPreference = findPreference(PreferenceKeys.KEY_RESET_SETTINGS);
        hotspotNamePreference = (EditTextPreference) findPreference(PreferenceKeys.KEY_HOTSPOT_NAME);
        hotspotPasswordPreference = findPreference(PreferenceKeys.KEY_HOTSPOT_PASSWORD);
        passwordRequirePreference = (CheckBoxPreference) findPreference(PreferenceKeys.KEY_HOTSPOT_PWD_REQUIRE);
        odkDestinationDirPreference = (EditTextPreference) findPreference(PreferenceKeys.KEY_ODK_DESTINATION_DIR);

        prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());

        hotspotNamePreference.setSummary(prefs.getString(PreferenceKeys.KEY_HOTSPOT_NAME,
                getString(R.string.default_hotspot_ssid)));
        boolean isPasswordSet = prefs.getBoolean(PreferenceKeys.KEY_HOTSPOT_PWD_REQUIRE, false);
        odkDestinationDirPreference.setSummary(prefs.getString(PreferenceKeys.KEY_ODK_DESTINATION_DIR,
                getString(R.string.default_odk_destination_dir)));

        hotspotPasswordPreference.setEnabled(isPasswordSet);
        passwordRequirePreference.setChecked(isPasswordSet);

        hotspotNamePreference.setOnPreferenceChangeListener(preferenceChangeListener());
        hotspotPasswordPreference.setOnPreferenceChangeListener(preferenceChangeListener());
        passwordRequirePreference.setOnPreferenceChangeListener(preferenceChangeListener());
        odkDestinationDirPreference.setOnPreferenceChangeListener(preferenceChangeListener());

        resetPreference.setOnPreferenceClickListener(preferenceClickListener());
        hotspotPasswordPreference.setOnPreferenceClickListener(preferenceClickListener());
    }

    private Preference.OnPreferenceClickListener preferenceClickListener() {
        return preference -> {
            switch (preference.getKey()) {
                case PreferenceKeys.KEY_HOTSPOT_PASSWORD:
                    showPasswordDialog();
                    break;
                case PreferenceKeys.KEY_RESET_SETTINGS:
                    resetApplication();
                    break;
            }
            return false;
        };
    }

    private Preference.OnPreferenceChangeListener preferenceChangeListener() {
        return (preference, newValue) -> {
            switch (preference.getKey()) {
                case PreferenceKeys.KEY_HOTSPOT_NAME:
                    String name = newValue.toString();
                    if (name.length() == 0) {
                        Toast.makeText(getBaseContext(), getString(R.string.hotspot_name_error), Toast.LENGTH_LONG).show();
                        return false;
                    } else {
                        hotspotNamePreference.setSummary(name);
                    }
                    break;
                case PreferenceKeys.KEY_HOTSPOT_PASSWORD:
                    String password = newValue.toString();
                    if (password.length() < 8) {
                        Toast.makeText(getBaseContext(), getString(R.string.hotspot_password_error), Toast.LENGTH_LONG).show();
                        return false;
                    }
                    break;
                case PreferenceKeys.KEY_HOTSPOT_PWD_REQUIRE:
                    boolean isRequire = (Boolean) newValue;
                    if (isRequire) {
                        hotspotPasswordPreference.setEnabled(true);
                    } else {
                        hotspotPasswordPreference.setEnabled(false);
                    }
                    break;
                case PreferenceKeys.KEY_ODK_DESTINATION_DIR:
                    String dir = newValue.toString();
                    if (dir.length() == 0) {
                        Toast.makeText(getApplicationContext(), getString(R.string.odk_destination_dir_error), Toast.LENGTH_LONG).show();
                        return false;
                    } else {
                        odkDestinationDirPreference.setSummary(dir);
                    }
                    break;
            }
            return true;
        };
    }

    private ViewGroup getRootView() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            return (ViewGroup) findViewById(android.R.id.list).getParent().getParent().getParent();
        } else {
            return (ViewGroup) findViewById(android.R.id.list).getParent();
        }
    }

    private void showPasswordDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater factory = LayoutInflater.from(this);

        View dialogView = factory.inflate(R.layout.dialog_password_til, null);
        TextInputLayout tlPassword = dialogView.findViewById(R.id.et_password_layout);
        tlPassword.getEditText().setText(prefs.getString(PreferenceKeys.KEY_HOTSPOT_PASSWORD, getString(R.string.default_hotspot_password)));

        builder.setTitle(getString(R.string.title_hotspot_password));
        builder.setView(dialogView);
        builder.setPositiveButton(getString(R.string.ok), (dialog, which) -> {
            String password = tlPassword.getEditText().getText().toString();
            prefs.edit().putString(PreferenceKeys.KEY_HOTSPOT_PASSWORD, password).apply();
        });
        builder.setNegativeButton(getString(R.string.cancel), (dialog, which) -> dialog.dismiss());

        builder.setCancelable(false);
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
        alertDialog.setCancelable(true);
        alertDialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
    }

    /**
     * Reset the application settings and the database.
     */
    private void resetApplication() {
        View checkBoxView = View.inflate(this, R.layout.pref_reset_dialog, null);
        CheckBox cbResetPref = checkBoxView.findViewById(R.id.cb_reset_pref);
        CheckBox cbResetData = checkBoxView.findViewById(R.id.cb_clear_db);

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        AlertDialog resetDialog = builder.setTitle(getString(R.string.title_reset_settings))
                .setView(checkBoxView)
                .setCancelable(false)
                .setPositiveButton(getString(R.string.ok), (DialogInterface dialog, int which) -> {
                    progressDialog = new ProgressDialog(this);
                    progressDialog.setTitle(getString(R.string.resetting));
                    progressDialog.setMessage(getString(R.string.resetting_msg));
                    progressDialog.setIndeterminate(true);
                    progressDialog.setCancelable(false);
                    progressDialog.show();
                    dialog.dismiss();

                    if (!cbResetData.isChecked() && !cbResetPref.isChecked()) {
                        progressDialog.dismiss();
                        Toast.makeText(this, getString(R.string.reset_select_nothing), Toast.LENGTH_LONG).show();
                    } else {
                        startRest(cbResetPref, cbResetData);
                    }
                })
                .setNegativeButton(getString(R.string.cancel), (DialogInterface dialog, int which) -> {
                    dialog.dismiss();
                })
                .create();
        resetDialog.show();
    }

    /**
     * Start resetting the application and presenting the reset result in an {@link AlertDialog}.
     */
    private void startRest(CheckBox cbResetPref, CheckBox cbResetData) {
        StringBuilder stringBuilder = new StringBuilder();
        if (cbResetData.isChecked()) {
            stringBuilder.append(getString(R.string.reset_result_data,
                    resetData() ? getString(R.string.reset_success) : getString(R.string.reset_failed)));
        }

        if (cbResetPref.isChecked()) {
            stringBuilder.append(getString(R.string.reset_result_pref,
                    resetPreference() ? getString(R.string.reset_success) : getString(R.string.reset_failed)));
        }

        progressDialog.dismiss();
        new AlertDialog.Builder(this)
                .setTitle(getString(R.string.reset_result))
                .setMessage(stringBuilder.toString())
                .setCancelable(false)
                .setPositiveButton(getString(R.string.ok), (DialogInterface dialog, int which) -> {
                    dialog.dismiss();
                    finish();
                })
                .create()
                .show();
    }

    /**
     * Reset the preference by resetting the {@link SharedPreferences}.
     */
    private boolean resetPreference() {
        SharedPreferences.Editor editor = prefs.edit();
        editor.clear();
        return editor.commit();
    }

    /**
     * Removing the cache and database table by deleting the share
     * folder and create a new one.
     */
    private boolean resetData() {
        boolean result = FileUtils.deleteFolderContents(Share.ODK_ROOT);
        Share.createODKDirs(this);
        return result;
    }
}
