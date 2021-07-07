package org.odk.share.views.ui.settings;

import android.bluetooth.BluetoothAdapter;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.EditTextPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.appcompat.widget.Toolbar;

import org.odk.share.R;

/**
 * Created by laksh on 5/27/2018.
 */

public class SettingsActivity extends PreferenceActivity {

    EditTextPreference hotspotNamePreference;
    EditTextPreference bluetoothNamePreference;
    Preference hotspotPasswordPreference;
    CheckBoxPreference passwordRequirePreference;
    CheckBoxPreference btSecureModePreference;
    CheckBoxPreference statisticsPreference;
    EditTextPreference odkDestinationDirPreference;
    ListPreference defaultMethodPreference;
    private SharedPreferences prefs;

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
        defaultMethodPreference = (ListPreference) findPreference(PreferenceKeys.KEY_DEFAULT_TRANSFER_METHOD);
        hotspotNamePreference = (EditTextPreference) findPreference(PreferenceKeys.KEY_HOTSPOT_NAME);
        bluetoothNamePreference = (EditTextPreference) findPreference(PreferenceKeys.KEY_BLUETOOTH_NAME);
        hotspotPasswordPreference = findPreference(PreferenceKeys.KEY_HOTSPOT_PASSWORD);
        passwordRequirePreference = (CheckBoxPreference) findPreference(PreferenceKeys.KEY_HOTSPOT_PWD_REQUIRE);
        btSecureModePreference = (CheckBoxPreference) findPreference(PreferenceKeys.KEY_BLUETOOTH_SECURE_MODE);
        odkDestinationDirPreference = (EditTextPreference) findPreference(PreferenceKeys.KEY_ODK_DESTINATION_DIR);
        statisticsPreference = (CheckBoxPreference) findPreference(PreferenceKeys.KEY_DETAILED_STATISTICS);

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
        odkDestinationDirPreference.setSummary(prefs.getString(PreferenceKeys.KEY_ODK_DESTINATION_DIR,
                getString(R.string.default_odk_destination_dir)));
        hotspotPasswordPreference.setEnabled(isPasswordSet);
        passwordRequirePreference.setChecked(isPasswordSet);
        boolean isSecureMode = prefs.getBoolean(PreferenceKeys.KEY_BLUETOOTH_SECURE_MODE, true);
        btSecureModePreference.setChecked(isSecureMode);
        boolean detailedStatisticsEnabled = prefs.getBoolean(PreferenceKeys.KEY_DETAILED_STATISTICS, false);
        statisticsPreference.setChecked(detailedStatisticsEnabled);

        hotspotNamePreference.setOnPreferenceChangeListener(preferenceChangeListener());
        bluetoothNamePreference.setOnPreferenceChangeListener(preferenceChangeListener());
        hotspotPasswordPreference.setOnPreferenceChangeListener(preferenceChangeListener());
        passwordRequirePreference.setOnPreferenceChangeListener(preferenceChangeListener());
        odkDestinationDirPreference.setOnPreferenceChangeListener(preferenceChangeListener());
        defaultMethodPreference.setOnPreferenceChangeListener(preferenceChangeListener());

        hotspotPasswordPreference.setOnPreferenceClickListener(preferenceClickListener());
    }

    private Preference.OnPreferenceClickListener preferenceClickListener() {
        return preference -> {
            switch (preference.getKey()) {
                case PreferenceKeys.KEY_HOTSPOT_PASSWORD:
                    SetHotspotPasswordDialog setHotspotPasswordDialog = SetHotspotPasswordDialog.newInstance();
                    setHotspotPasswordDialog.show(getFragmentManager(), "set hotspot password dialog");
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
                case PreferenceKeys.KEY_ODK_DESTINATION_DIR:
                    String dir = newValue.toString();
                    if (dir.length() == 0) {
                        Toast.makeText(getApplicationContext(), getString(R.string.odk_destination_dir_error), Toast.LENGTH_LONG).show();
                        return false;
                    } else {
                        odkDestinationDirPreference.setSummary(dir);
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
}

