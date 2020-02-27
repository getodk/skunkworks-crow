package org.odk.share.utilities;

import android.graphics.Bitmap;

import com.google.zxing.WriterException;

import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

import java.io.IOException;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

@RunWith(RobolectricTestRunner.class)
public class QRCodeUtilsTest {

    /**
     * {@link Test} create Hotspot Info of {@link QRCodeUtils}
     */
    @Test
    public void createHotspotInfoTest() throws JSONException {
        String ssid = "DeviceSSID";
        int port = 3;
        String password = "password";
        String hotspotInfo = QRCodeUtils.createHotspotInfo(ssid, port, password);
        JSONObject hotspotInfoJson = new JSONObject(hotspotInfo);
        assertTrue(hotspotInfoJson.get(QRCodeUtils.SSID).equals(ssid));
        assertTrue(hotspotInfoJson.getInt(QRCodeUtils.PORT) == port);
        assertTrue(hotspotInfoJson.getBoolean(QRCodeUtils.PROTECTED));
        assertTrue(hotspotInfoJson.get(QRCodeUtils.PASSWORD).equals(password));

        password = null;
        hotspotInfo = QRCodeUtils.createHotspotInfo(ssid, port, password);
        hotspotInfoJson = new JSONObject(hotspotInfo);
        assertTrue(hotspotInfoJson.get(QRCodeUtils.SSID).equals(ssid));
        assertFalse(hotspotInfoJson.getBoolean(QRCodeUtils.PROTECTED));
        assertTrue(hotspotInfoJson.getInt(QRCodeUtils.PORT) == port);
    }

    /**
     * {@link Test} generate QR bitmap of {@link QRCodeUtils}
     */
    @Test
    public void generateQRBitMapTest() throws IOException, WriterException {
        String data = "Test Bitmap";
        int sideLength = QRCodeUtils.SIDE_LENGTH;
        Bitmap bitmap = QRCodeUtils.generateQRBitMap(data, sideLength);
        assertTrue(bitmap.getHeight() == sideLength);
        assertTrue(bitmap.getWidth() == sideLength);
    }
}
