package org.odk.share.preferences;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import org.odk.share.R;
import org.odk.share.injection.config.scopes.PerApplication;

import javax.inject.Inject;

@PerApplication

public class SharedPreferencesHelper {

    private final SharedPreferences sharedPreferences;
    private final Context context;

    @Inject
    SharedPreferencesHelper(Context context) {
        this.context = context;
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
    }

    public String getHotspotName() {
        return sharedPreferences.getString(PreferenceKeys.KEY_HOTSPOT_NAME, "");
    }

    public void setHotspotName(String hotspotName) {
        sharedPreferences
                .edit()
                .putString(PreferenceKeys.KEY_HOTSPOT_NAME, hotspotName)
                .apply();
    }

    public String getHotspotPassword() {
        return sharedPreferences.getString(PreferenceKeys.KEY_HOTSPOT_PASSWORD, "");
    }

    public void setHotspotPassword(String hotspotPassword) {
        sharedPreferences
                .edit()
                .putString(PreferenceKeys.KEY_HOTSPOT_PASSWORD, hotspotPassword)
                .apply();
    }

    public void setHotspotPasswordRequired(boolean isRequired) {
        sharedPreferences
                .edit()
                .putBoolean(PreferenceKeys.KEY_HOTSPOT_PWD_REQUIRE, isRequired)
                .apply();
    }

    public boolean isHotspotPasswordProtected() {
        return sharedPreferences.getBoolean(PreferenceKeys.KEY_HOTSPOT_PWD_REQUIRE, false);
    }

    public String getDestinationDir() {
        return sharedPreferences.getString(PreferenceKeys.KEY_ODK_DESTINATION_DIR, context.getString(R.string.default_odk_destination_dir));
    }
}
