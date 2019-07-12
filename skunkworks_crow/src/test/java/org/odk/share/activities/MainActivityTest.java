package org.odk.share.activities;


import android.content.Intent;
import android.view.Menu;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.widget.Toolbar;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.odk.share.R;
import org.odk.share.views.ui.about.AboutActivity;
import org.odk.share.views.ui.bluetooth.BtReceiverActivity;
import org.odk.share.views.ui.hotspot.HpReceiverActivity;
import org.odk.share.views.ui.main.MainActivity;
import org.odk.share.views.ui.send.SendFormsActivity;
import org.odk.share.views.ui.settings.SettingsActivity;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.shadows.ShadowActivity;
import org.robolectric.shadows.ShadowAlertDialog;
import org.robolectric.shadows.ShadowIntent;

import static junit.framework.TestCase.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.robolectric.Shadows.shadowOf;

@RunWith(RobolectricTestRunner.class)
public class MainActivityTest {

    private MainActivity mainActivity;

    @Before
    public void setUp() throws Exception {
        mainActivity = Robolectric.setupActivity(MainActivity.class);
    }

    /**
     * {@link Test} to assert {@link MainActivity} for not null.
     */
    @Test
    public void shouldNotBeNull() {
        assertNotNull(mainActivity);
    }

    /**
     * {@link Test} to assert title of {@link MainActivity} for not null.
     */
    @Test
    public void titleTest() throws Exception {
        Toolbar toolbar = mainActivity.findViewById(R.id.toolbar);
        assertEquals(mainActivity.getString(R.string.app_name), toolbar.getTitle());
    }

    /**
     * {@link Test} to assert receiveButton's functioning.
     */
    @Test
    public void receiveButtonTest() throws Exception {
        Button receiveButton = mainActivity.findViewById(R.id.bReceiveForms);

        assertNotNull(receiveButton);
        assertEquals(View.VISIBLE, receiveButton.getVisibility());
        assertEquals(mainActivity.getString(R.string.receive_forms), receiveButton.getText());

        receiveButton.performClick();
        android.app.AlertDialog alertDialog = (android.app.AlertDialog) ShadowAlertDialog.getLatestDialog();
        ShadowAlertDialog shadowAlertDialog = shadowOf(alertDialog);
        assertEquals(shadowAlertDialog.getTitle(), "Receive Options");
        assertEquals(shadowAlertDialog.getItems().length, 2);
        assertEquals(shadowAlertDialog.getItems()[0], "Bluetooth");
        assertEquals(shadowAlertDialog.getItems()[1], "Hotspot");

        shadowAlertDialog.clickOnItem(0);
        Intent expectedBluetoothIntent = new Intent(mainActivity, BtReceiverActivity.class);
        Intent bluetoothActual = shadowOf(RuntimeEnvironment.application).getNextStartedActivity();
        assertEquals(expectedBluetoothIntent.getComponent(), bluetoothActual.getComponent());

        shadowAlertDialog.clickOnItem(1);
        Intent expectedHotspotIntent = new Intent(mainActivity, HpReceiverActivity.class);
        Intent hotspotActual = shadowOf(RuntimeEnvironment.application).getNextStartedActivity();
        assertEquals(expectedHotspotIntent.getComponent(), hotspotActual.getComponent());
    }

    /**
     * {@link Test} to assert sendButton's functioning.
     */
    @Test
    public void sendButtonTest() throws Exception {
        Button sendButton = mainActivity.findViewById(R.id.bSendForms);

        assertNotNull(sendButton);
        assertEquals(View.VISIBLE, sendButton.getVisibility());
        assertEquals(mainActivity.getString(R.string.send_forms), sendButton.getText());

        sendButton.performClick();
        ShadowActivity shadowActivity = shadowOf(mainActivity);
        Intent startedIntent = shadowActivity.getNextStartedActivity();
        ShadowIntent shadowIntent = shadowOf(startedIntent);
        assertEquals(SendFormsActivity.class.getName(),
                shadowIntent.getIntentClass().getName());
    }

    /**
     * {@link Test} to assert Options Menu's functioning.
     */
    @Test
    public void optionsMenuTest() throws Exception {
        Menu menu = shadowOf(mainActivity).getOptionsMenu();
        assertNotNull(menu);

        //Test for SettingsActivity
        mainActivity.onOptionsItemSelected(menu.getItem(0));
        ShadowActivity shadowActivity = shadowOf(mainActivity);
        Intent startedIntent = shadowActivity.getNextStartedActivity();
        ShadowIntent shadowIntent = shadowOf(startedIntent);
        assertEquals(SettingsActivity.class.getName(), shadowIntent.getIntentClass().getName());

        //Test for Settings Menu Title
        String menuTitle = mainActivity.getResources().getString(R.string.settings);
        String shadowTitle = menu.getItem(0).getTitle().toString();
        assertEquals(shadowTitle, menuTitle);

        //Test for AboutActivity
        mainActivity.onOptionsItemSelected(menu.getItem(1));
        shadowActivity = shadowOf(mainActivity);
        startedIntent = shadowActivity.getNextStartedActivity();
        shadowIntent = shadowOf(startedIntent);
        assertEquals(AboutActivity.class.getName(), shadowIntent.getIntentClass().getName());

        //Test for About Menu Title
        menuTitle = mainActivity.getResources().getString(R.string.about);
        shadowTitle = menu.getItem(1).getTitle().toString();
        assertEquals(shadowTitle, menuTitle);
    }
}
