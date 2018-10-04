package org.odk.share.preferences;

import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.EditTextPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import org.odk.share.R;
import org.odk.share.application.Share;


/**
 * Created by laksh on 5/27/2018.
 */

public class SettingsPreference extends PreferenceActivity {

    EditTextPreference hotspotNamePreference;
    EditTextPreference hotspotPasswordPreference;
    CheckBoxPreference passwordRequirePreference;
    EditTextPreference odkDestinationDirPreference;

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
        hotspotNamePreference = (EditTextPreference) findPreference(PreferenceKeys.KEY_HOTSPOT_NAME);
        hotspotPasswordPreference = (EditTextPreference) findPreference(PreferenceKeys.KEY_HOTSPOT_PASSWORD);
        passwordRequirePreference = (CheckBoxPreference) findPreference(PreferenceKeys.KEY_HOTSPOT_PWD_REQUIRE);
        odkDestinationDirPreference = (EditTextPreference) findPreference(PreferenceKeys.KEY_ODK_DESTINATION_DIR);

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());

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
    }

    private Preference.OnPreferenceChangeListener preferenceChangeListener() {
        return (preference, newValue) -> {
            switch (preference.getKey()) {
                case PreferenceKeys.KEY_HOTSPOT_NAME:
                    String name = newValue.toString();
                    if (name.length() == 0) {
                        Toast.makeText(Share.getInstance(), getString(R.string.hotspot_name_error), Toast.LENGTH_LONG).show();
                        return false;
                    }
                    break;
                case PreferenceKeys.KEY_HOTSPOT_PASSWORD:
                    String password = newValue.toString();
                    if (password.length() < 8) {
                        Toast.makeText(Share.getInstance(), getString(R.string.hotspot_password_error), Toast.LENGTH_LONG).show();
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
                        Toast.makeText(Share.getInstance(), getString(R.string.odk_destination_dir_error), Toast.LENGTH_LONG).show();
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

}