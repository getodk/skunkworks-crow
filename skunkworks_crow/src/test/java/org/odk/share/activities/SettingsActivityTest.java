package org.odk.share.activities;

import android.content.SharedPreferences;
import android.preference.CheckBoxPreference;
import android.preference.EditTextPreference;
import android.preference.Preference;
import android.preference.PreferenceManager;

import androidx.appcompat.widget.Toolbar;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.odk.share.R;
import org.odk.share.views.ui.settings.PreferenceKeys;
import org.odk.share.views.ui.settings.SettingsActivity;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;

import static junit.framework.TestCase.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

@RunWith(RobolectricTestRunner.class)
public class SettingsActivityTest {

    private SettingsActivity settingsActivity;
    private SharedPreferences prefs;
    private Preference hotspotPasswordPreference;
    private CheckBoxPreference passwordRequirePreference;
    private EditTextPreference hotspotNamePreference;
    private EditTextPreference odkDestinationDirPreference;

    @Before
    public void setUp() throws Exception {
        settingsActivity = Robolectric.buildActivity(SettingsActivity.class).create().get();
        prefs = PreferenceManager.getDefaultSharedPreferences(RuntimeEnvironment.application.getApplicationContext());
    }

    /**
     * {@link Test} to assert {@link SettingsActivity} for not null.
     */
    @Test
    public void shouldNotBeNull() {
        assertNotNull(settingsActivity);
    }

    /**
     * {@link Test} to assert title of {@link SettingsActivity} for not null.
     */
    @Test
    public void titleTest() throws Exception {
        Toolbar toolbar = settingsActivity.findViewById(R.id.toolbar);
        assertEquals(settingsActivity.getString(R.string.settings), toolbar.getTitle());
    }

    /**
     * {@link Test} the hotspot name preference.
     */
    @Test
    public void preferenceMenuTest() {
        //add the preference menu
        settingsActivity.addPreferencesFromResource(R.xml.preferences_menu);

        //test for hotspotNamePreference
        assertNotNull(settingsActivity.findPreference(PreferenceKeys.KEY_HOTSPOT_NAME));
        //test for hotspotPasswordPreference
        assertNotNull(settingsActivity.findPreference(PreferenceKeys.KEY_HOTSPOT_PASSWORD));
        //test for passwordRequirePreference
        assertNotNull(settingsActivity.findPreference(PreferenceKeys.KEY_HOTSPOT_PWD_REQUIRE));
        //test for odkDestinationDirPreference
        assertNotNull(settingsActivity.findPreference(PreferenceKeys.KEY_ODK_DESTINATION_DIR));
    }

    /**
     * {@link Test} the preference summary.
     */
    @Test
    public void preferenceSummaryTest() {
        hotspotNamePreference = (EditTextPreference) settingsActivity.findPreference(PreferenceKeys.KEY_HOTSPOT_NAME);
        odkDestinationDirPreference = (EditTextPreference) settingsActivity.findPreference(PreferenceKeys.KEY_ODK_DESTINATION_DIR);

        //test the summary
        assertEquals(prefs.getString(PreferenceKeys.KEY_HOTSPOT_NAME,
                settingsActivity.getString(R.string.default_hotspot_ssid)), hotspotNamePreference.getSummary());

        assertEquals(prefs.getString(PreferenceKeys.KEY_ODK_DESTINATION_DIR,
                settingsActivity.getString(R.string.default_odk_destination_dir)), odkDestinationDirPreference.getSummary());
    }

    /**
     * {@link Test} the preference status, like {@link android.widget.CheckBox}, and whether enable or not.
     */
    @Test
    public void preferenceStatusTest() {
        hotspotPasswordPreference = settingsActivity.findPreference(PreferenceKeys.KEY_HOTSPOT_PASSWORD);
        passwordRequirePreference = (CheckBoxPreference) settingsActivity.findPreference(PreferenceKeys.KEY_HOTSPOT_PWD_REQUIRE);

        hotspotPasswordPreference.setEnabled(false);
        assertFalse(hotspotPasswordPreference.isEnabled());
        hotspotPasswordPreference.setEnabled(true);
        assertTrue(hotspotPasswordPreference.isEnabled());

        passwordRequirePreference.setChecked(false);
        assertFalse(passwordRequirePreference.isChecked());
        passwordRequirePreference.setChecked(true);
        assertTrue(passwordRequirePreference.isChecked());
    }
}