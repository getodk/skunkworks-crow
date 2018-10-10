package org.odk.share.utilities;

import org.odk.share.application.Share;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class OdkInstanceUtils {
    /**
     * Uniquely identifiable ID for an instance would be a combination of the name of the directory
     * for the instance and the device id.
     * @return
     */
    public static String getInstanceUuid(String instancePath) throws UnsupportedEncodingException, NoSuchAlgorithmException {
        return SHA1(getDeviceId() + instancePath);
    }

    private static String convertToHex(byte[] data) {
        StringBuilder buf = new StringBuilder();
        for (byte b : data) {
            int halfbyte = (b >>> 4) & 0x0F;
            int two_halfs = 0;
            do {
                buf.append((0 <= halfbyte) && (halfbyte <= 9) ? (char) ('0' + halfbyte) : (char) ('a' + (halfbyte - 10)));
                halfbyte = b & 0x0F;
            } while (two_halfs++ < 1);
        }
        return buf.toString();
    }

    private static String SHA1(String text) throws NoSuchAlgorithmException, UnsupportedEncodingException {
        MessageDigest md = MessageDigest.getInstance("SHA-1");
        byte[] textBytes = text.getBytes("iso-8859-1");
        md.update(textBytes, 0, textBytes.length);
        byte[] sha1hash = md.digest();
        return convertToHex(sha1hash);
    }

    /**
     * Same logic used within ODK Collect to obtain the device id in a form
     * @return
     */
    private static String getDeviceId() {
        PropertyManager propertyManager = new PropertyManager(Share.getInstance());
        return propertyManager.getSingularProperty(PropertyManager.DEVICE_ID_PROPERTY);
    }
}
