package org.odk.share.views.ui.settings;

import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.EditTextPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceCategory;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.Toolbar;

import com.google.android.material.textfield.TextInputLayout;

import org.odk.share.R;

import timber.log.Timber;


/**
 * Created by laksh on 5/27/2018.
 */

public class SettingsActivity extends PreferenceActivity {

    EditTextPreference hotspotNamePreference;
    EditTextPreference bluetoothNamePreference;
    Preference hotspotPasswordPreference;
    CheckBoxPreference passwordRequirePreference;
    CheckBoxPreference btSecureModePreference;
    EditTextPreference odkDestinationDirPreferenceEditText;
    ListPreference defaultMethodPreference;
    Preference odkDestinationDirPreferenceDirectoryPicker;
    private SharedPreferences prefs;

    private static final int DIRECTORY_REQUEST_CODE = 9999;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ViewGroup root = getRootView();
        Toolbar toolbar = (Toolbar) View.inflate(this, R.layout.toolbar, null);
        toolbar.setTitle(getString(R.string.settings));
        root.addView(toolbar, 0);

        addPreferencesFromResource(R.xml.preferences_menu);
        addPreferences();

        PreferenceCategory preferenceCategory = (PreferenceCategory) findPreference(PreferenceKeys.KEY_ODK_SETTINGS);
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.KITKAT) {
            preferenceCategory.removePreference(odkDestinationDirPreferenceEditText);
        } else {
            preferenceCategory.removePreference(odkDestinationDirPreferenceDirectoryPicker);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
        }
        return super.onOptionsItemSelected(item);
    }

    private void addPreferences() {
        defaultMethodPreference = (ListPreference) findPreference(PreferenceKeys.KEY_DEFAULT_TRANSFER_METHOD);
        hotspotNamePreference = (EditTextPreference) findPreference(PreferenceKeys.KEY_HOTSPOT_NAME);
        bluetoothNamePreference = (EditTextPreference) findPreference(PreferenceKeys.KEY_BLUETOOTH_NAME);
        hotspotPasswordPreference = findPreference(PreferenceKeys.KEY_HOTSPOT_PASSWORD);
        passwordRequirePreference = (CheckBoxPreference) findPreference(PreferenceKeys.KEY_HOTSPOT_PWD_REQUIRE);
        btSecureModePreference = (CheckBoxPreference) findPreference(PreferenceKeys.KEY_BLUETOOTH_SECURE_MODE);
        odkDestinationDirPreferenceEditText = (EditTextPreference) findPreference(PreferenceKeys.KEY_ODK_DESTINATION_DIR_EDIT_TEXT);
        odkDestinationDirPreferenceDirectoryPicker = (Preference) findPreference(PreferenceKeys.KEY_ODK_DESTINATION_DIR_DIRECTORY_PICKER);

        prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());

        defaultMethodPreference.setSummary(prefs.getString(PreferenceKeys.KEY_DEFAULT_TRANSFER_METHOD,
                getString(R.string.default_hotspot_ssid)));
        hotspotNamePreference.setSummary(prefs.getString(PreferenceKeys.KEY_HOTSPOT_NAME,
                getString(R.string.default_hotspot_ssid)));
        String defaultBluetoothName = BluetoothAdapter.getDefaultAdapter().getName();
        bluetoothNamePreference.setText(defaultBluetoothName);
        bluetoothNamePreference.setDefaultValue(defaultBluetoothName);
        bluetoothNamePreference.setSummary(prefs.getString(PreferenceKeys.KEY_BLUETOOTH_NAME, defaultBluetoothName));
        boolean isPasswordSet = prefs.getBoolean(PreferenceKeys.KEY_HOTSPOT_PWD_REQUIRE, false);


        odkDestinationDirPreferenceDirectoryPicker.setSummary(prefs.getString(PreferenceKeys.KEY_ODK_DESTINATION_DIR_DIRECTORY_PICKER, getString(R.string.default_odk_destination_dir)));
        odkDestinationDirPreferenceEditText.setSummary(prefs.getString(PreferenceKeys.KEY_ODK_DESTINATION_DIR_EDIT_TEXT,
                getString(R.string.default_odk_destination_dir)));
        boolean isSecureMode = prefs.getBoolean(PreferenceKeys.KEY_BLUETOOTH_SECURE_MODE, true);

        hotspotPasswordPreference.setEnabled(isPasswordSet);
        passwordRequirePreference.setChecked(isPasswordSet);
        btSecureModePreference.setChecked(isSecureMode);

        hotspotNamePreference.setOnPreferenceChangeListener(preferenceChangeListener());
        bluetoothNamePreference.setOnPreferenceChangeListener(preferenceChangeListener());
        hotspotPasswordPreference.setOnPreferenceChangeListener(preferenceChangeListener());
        passwordRequirePreference.setOnPreferenceChangeListener(preferenceChangeListener());
        odkDestinationDirPreferenceEditText.setOnPreferenceChangeListener(preferenceChangeListener());
        odkDestinationDirPreferenceDirectoryPicker.setOnPreferenceClickListener(preferenceClickListener());
        defaultMethodPreference.setOnPreferenceChangeListener(preferenceChangeListener());

        hotspotPasswordPreference.setOnPreferenceClickListener(preferenceClickListener());
    }

    private Preference.OnPreferenceClickListener preferenceClickListener() {
        return preference -> {
            switch (preference.getKey()) {
                case PreferenceKeys.KEY_HOTSPOT_PASSWORD:
                    showPasswordDialog();
                    break;
                case PreferenceKeys.KEY_ODK_DESTINATION_DIR_DIRECTORY_PICKER:
                    chooseDirectory();
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
                case PreferenceKeys.KEY_BLUETOOTH_NAME:
                    String bluetoothName = newValue.toString();
                    if (bluetoothName.length() == 0) {
                        Toast.makeText(getBaseContext(), getString(R.string.bluetooth_name_error), Toast.LENGTH_LONG).show();
                        return false;
                    } else {
                        bluetoothNamePreference.setSummary(bluetoothName);
                        BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
                        bluetoothAdapter.setName(bluetoothName);
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
                case PreferenceKeys.KEY_ODK_DESTINATION_DIR_EDIT_TEXT:
                    String dir = newValue.toString();
                    if (dir.length() == 0) {
                        Toast.makeText(getApplicationContext(), getString(R.string.odk_destination_dir_error), Toast.LENGTH_LONG).show();
                        return false;
                    } else {
                        odkDestinationDirPreferenceEditText.setSummary(dir);
                        prefs.edit().putString(PreferenceKeys.KEY_ODK_DESTINATION_DIR_DIRECTORY_PICKER, null).apply();
                    }
                    break;
                case PreferenceKeys.KEY_DEFAULT_TRANSFER_METHOD:
                    String method = newValue.toString();
                    if (!TextUtils.isEmpty(method)) {
                        defaultMethodPreference.setSummary(method);
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

    public void chooseDirectory() {
        Intent i = new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE);
        i.addCategory(Intent.CATEGORY_DEFAULT);
        startActivityForResult(Intent.createChooser(i, getString(R.string.choose_directory)), DIRECTORY_REQUEST_CODE);
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case DIRECTORY_REQUEST_CODE:
                try {
                    String filePath = data.getData().getPath() + getString(R.string.directory_odk);
                    prefs.edit().putString(PreferenceKeys.KEY_ODK_DESTINATION_DIR_DIRECTORY_PICKER, filePath).apply();
                    prefs.edit().putString(PreferenceKeys.KEY_ODK_DESTINATION_DIR_EDIT_TEXT, null).apply();
                    odkDestinationDirPreferenceDirectoryPicker.setSummary(prefs.getString(PreferenceKeys.KEY_ODK_DESTINATION_DIR_DIRECTORY_PICKER, getString(R.string.default_odk_destination_dir)));
                } catch (Exception e) {
                    Timber.e("Can not choose the Directory");
                }
                break;
        }
    }
}

