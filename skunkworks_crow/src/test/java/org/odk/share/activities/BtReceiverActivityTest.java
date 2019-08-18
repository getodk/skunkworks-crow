package org.odk.share.activities;


import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Intent;

import androidx.appcompat.widget.Toolbar;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.odk.share.R;
import org.odk.share.bluetooth.BluetoothReceiver;
import org.odk.share.views.ui.bluetooth.BtReceiverActivity;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.shadows.ShadowApplication;
import org.robolectric.shadows.ShadowLog;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

@RunWith(RobolectricTestRunner.class)
public class BtReceiverActivityTest {

    static {
        ShadowLog.stream = System.out;
    }

    private BtReceiverActivity btReceiverActivity;

    @Before
    public void setUp() throws Exception {
        btReceiverActivity = Robolectric.setupActivity(BtReceiverActivity.class);
    }

    /**
     * {@link Test} to assert {@link BtReceiverActivity} for not null.
     */
    @Test
    public void shouldNotBeNull() {
        assertNotNull(btReceiverActivity);
    }

    /**
     * {@link Test} to assert title of {@link BtReceiverActivity} for not null.
     */
    @Test
    public void titleTest() throws Exception {
        Toolbar toolbar = btReceiverActivity.findViewById(R.id.toolbar);
        assertEquals(" " + btReceiverActivity.getString(R.string.connect_bluetooth_title), toolbar.getTitle());
    }

    /**
     * {@link Test} the {@link BluetoothReceiver} is correctly load in our tests.
     */
    @Test
    public void testBroadcastReceiverRegistered() {
        List<ShadowApplication.Wrapper> registeredReceivers = ShadowApplication.getInstance().getRegisteredReceivers();

        assertFalse(registeredReceivers.isEmpty());

        boolean receiverFound = false;
        for (ShadowApplication.Wrapper wrapper : registeredReceivers) {
            if (!receiverFound) {
                receiverFound = BluetoothReceiver.class.getSimpleName().equals(
                        wrapper.broadcastReceiver.getClass().getSimpleName());
            }
        }

        assertTrue(receiverFound); //will be false if not found
    }

    /**
     * {@link Test} to check if we have receivers listening to the defined action.
     */
    @Test
    public void testIntentHandling() {
        ShadowApplication shadowApplication = ShadowApplication.getInstance();
        Intent intent = new Intent();
        List<BroadcastReceiver> receiversForIntent;

        // ACTION_DISCOVERY_STARTED
        intent.setAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED);
        receiversForIntent = shadowApplication.getReceiversForIntent(intent);
        assertTrue(shadowApplication.hasReceiverForIntent(intent));
        assertEquals("Expected one broadcast receiver", 1, receiversForIntent.size());

        // ACTION_DISCOVERY_FINISHED
        intent.setAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        receiversForIntent = shadowApplication.getReceiversForIntent(intent);
        assertTrue(shadowApplication.hasReceiverForIntent(intent));
        assertEquals("Expected one broadcast receiver", 1, receiversForIntent.size());

        // ACTION_FOUND
        intent.setAction(BluetoothDevice.ACTION_FOUND);
        receiversForIntent = shadowApplication.getReceiversForIntent(intent);
        assertTrue(shadowApplication.hasReceiverForIntent(intent));
        assertEquals("Expected one broadcast receiver", 1, receiversForIntent.size());
    }
}
