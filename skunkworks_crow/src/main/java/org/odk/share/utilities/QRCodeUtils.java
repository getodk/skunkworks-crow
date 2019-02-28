package org.odk.share.utilities;

import android.graphics.Bitmap;
import android.graphics.Color;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import io.reactivex.Observable;

/**
 * Created by laksh on 6/9/2018.
 */

public class QRCodeUtils {

    public static final String SSID = "ssid";
    public static final String PORT = "port";
    public static final String PROTECTED = "protected";
    public static final String PASSWORD = "password";

    private QRCodeUtils() {
    }

    public static Observable<Bitmap> generateQRCode(String ssid, int port, String password) {
        return Observable.create(emitter -> {
            String qrCodeInfo = createHotspotInfo(ssid, port, password);
            emitter.onNext(generateQRBitMap(qrCodeInfo, 400));
            emitter.onComplete();
        });
    }

    public static String createHotspotInfo(String ssid, int port, String password) throws JSONException {
        JSONObject hotspotQRCode = new JSONObject();
        hotspotQRCode.put(SSID, ssid);
        hotspotQRCode.put(PORT, port);

        if (password == null) {
            hotspotQRCode.put(PROTECTED, false);
        } else {
            hotspotQRCode.put(PROTECTED, true);
            hotspotQRCode.put(PASSWORD, password);
        }
        return hotspotQRCode.toString();
    }

    public static Bitmap generateQRBitMap(String data, int sideLength) throws IOException, WriterException {
        Map<EncodeHintType, ErrorCorrectionLevel> hints = new HashMap<>();
        hints.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.L);
        QRCodeWriter qrCodeWriter = new QRCodeWriter();
        BitMatrix bitMatrix = qrCodeWriter.encode(data, BarcodeFormat.QR_CODE, sideLength, sideLength, hints);

        Bitmap bmp = Bitmap.createBitmap(sideLength, sideLength, Bitmap.Config.RGB_565);
        for (int x = 0; x < sideLength; x++) {
            for (int y = 0; y < sideLength; y++) {
                bmp.setPixel(x, y, bitMatrix.get(x, y) ? Color.BLACK : Color.WHITE);
            }
        }

        return bmp;
    }
}
