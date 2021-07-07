package org.odk.share.views.ui.receive;

/*
 * created by Chromicle
 */

// I got resource from one of stackoverflow project https://stackoverflow.com/questions/32731869/turn-on-off-flashlight-in-zxing-fragment-lib/43886809l

import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;

import com.journeyapps.barcodescanner.CaptureManager;
import com.journeyapps.barcodescanner.DecoratedBarcodeView;

import org.odk.share.R;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;


public class ScannerActivity extends AppCompatActivity implements
        DecoratedBarcodeView.TorchListener {

    @BindView(R.id.switch_flashlight)
    Button toggleFlashlightButton;
    @BindView(R.id.zxing_barcode_scanner)
    DecoratedBarcodeView decoratedScannerView;
    private CaptureManager capture;
    private boolean isFlashLightOn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scanner);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);

        ButterKnife.bind(this);
        decoratedScannerView.setTorchListener(this);


        if (!isFlashlightSupported()) {
            toggleFlashlightButton.setVisibility(View.GONE);
        }

        capture = new CaptureManager(this, decoratedScannerView);
        capture.initializeFromIntent(getIntent(), savedInstanceState);
        capture.decode();
    }

    private boolean isFlashlightSupported() {
        return getApplicationContext().getPackageManager()
                .hasSystemFeature(PackageManager.FEATURE_CAMERA_FLASH);
    }

    public void toggleFlashlight() {
        if (isFlashLightOn) {
            decoratedScannerView.setTorchOff();
            isFlashLightOn = false;
        } else {
            decoratedScannerView.setTorchOn();
            isFlashLightOn = true;
        }
    }

    @Override
    public void onTorchOn() {
        toggleFlashlightButton.setBackgroundResource(R.drawable.ic_flash_white_on);
    }

    @Override
    public void onTorchOff() {
        toggleFlashlightButton.setBackgroundResource(R.drawable.ic_flash_white_off);
    }

    @Override
    protected void onResume() {
        super.onResume();
        capture.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        capture.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        capture.onDestroy();
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        capture.onSaveInstanceState(outState);
    }

    @OnClick(R.id.switch_flashlight)
    public void toggleButton() {
        toggleFlashlight();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        return decoratedScannerView.onKeyDown(keyCode, event) || super.onKeyDown(keyCode, event);
    }

}