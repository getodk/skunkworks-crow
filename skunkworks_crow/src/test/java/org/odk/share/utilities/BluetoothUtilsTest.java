package org.odk.share.utilities;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.shadow.api.Shadow;
import org.robolectric.shadows.ShadowBluetoothAdapter;
import org.robolectric.shadows.ShadowBluetoothDevice;

import java.util.HashSet;
import java.util.Set;

import static android.bluetooth.BluetoothAdapter.STATE_OFF;
import static android.bluetooth.BluetoothAdapter.STATE_ON;
import static android.bluetooth.BluetoothAdapter.STATE_TURNING_OFF;
import static android.bluetooth.BluetoothAdapter.STATE_TURNING_ON;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.robolectric.Shadows.shadowOf;


/**
 * {@link Test} for basic operation related to {@link BluetoothAdapter} and bluetooth utilities.
 */
@RunWith(RobolectricTestRunner.class)
public class BluetoothUtilsTest {

    private BluetoothAdapter bluetoothAdapter;
    private ShadowBluetoothAdapter shadowBluetoothAdapter;

    @Before
    public void setUp() throws Exception {
        bluetoothAdapter = Shadow.newInstanceOf(BluetoothAdapter.class);
        shadowBluetoothAdapter = shadowOf(bluetoothAdapter);
    }

    /**
     * {@link Test} the enable method of {@link BluetoothAdapter}.
     */
    @Test
    public void enableBluetoothTest() {
        shadowBluetoothAdapter.setEnabled(true);
        assertTrue(bluetoothAdapter.isEnabled());
    }

    /**
     * {@link Test} the disable method of {@link BluetoothAdapter}.
     */
    @Test
    public void disableBluetoothTest() {
        shadowBluetoothAdapter.setEnabled(false);
        assertFalse(bluetoothAdapter.isEnabled());
    }

    /**
     * {@link Test} the bluetooth adapter no null, that support the bluetooth feature.
     */
    @Test
    public void supportBluetoothTest() {
        assertNotNull(bluetoothAdapter);
        assertNotNull(shadowBluetoothAdapter);
    }

    /**
     * {@link Test} the bluetooth status.
     */
    @Test
    public void bluetoothStatusTest() {
        // STATE_OFF
        shadowBluetoothAdapter.setState(STATE_OFF);
        assertEquals(STATE_OFF, bluetoothAdapter.getState());

        // STATE_ON
        shadowBluetoothAdapter.setState(STATE_ON);
        assertEquals(STATE_ON, bluetoothAdapter.getState());

        // STATE_TURNING_ON
        shadowBluetoothAdapter.setState(STATE_TURNING_ON);
        assertEquals(STATE_TURNING_ON, bluetoothAdapter.getState());

        // STATE_TURNING_OFF
        shadowBluetoothAdapter.setState(STATE_TURNING_OFF);
        assertEquals(STATE_TURNING_OFF, bluetoothAdapter.getState());
    }

    /**
     * {@link Test} the bonded {@link BluetoothDevice} from {@link BluetoothAdapter}.
     */
    @Test
    public void bondedDevicesTest() {
        Set<BluetoothDevice> bondedDevices = new HashSet<>();
        bondedDevices.add(ShadowBluetoothDevice.newInstance("18:83:31:43:2F:49"));
        bondedDevices.add(ShadowBluetoothDevice.newInstance("1C:CD:E5:DC:06:35"));
        bondedDevices.add(ShadowBluetoothDevice.newInstance("30:76:6F:7B:D5:B4"));

        shadowBluetoothAdapter.setBondedDevices(bondedDevices);
        assertEquals(bondedDevices, bluetoothAdapter.getBondedDevices());
    }

    /**
     * {@link Test} the bluetooth address of devices.
     */
    @Test
    public void bluetoothAddressTest() {
        shadowBluetoothAdapter.setAddress("A0:8D:16:F3:97:4D");
        assertEquals("A0:8D:16:F3:97:4D", bluetoothAdapter.getAddress());

        shadowBluetoothAdapter.setAddress("5A:61:C9:69:D0:3C");
        assertEquals("5A:61:C9:69:D0:3C", bluetoothAdapter.getAddress());

        shadowBluetoothAdapter.setAddress("74:AC:5F:D6:51:D3");
        assertEquals("74:AC:5F:D6:51:D3", bluetoothAdapter.getAddress());
    }
}
