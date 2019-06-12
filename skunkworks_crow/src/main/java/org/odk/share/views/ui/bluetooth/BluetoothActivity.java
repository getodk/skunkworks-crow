package org.odk.share.views.ui.bluetooth;

import android.os.Bundle;

import org.odk.share.R;
import org.odk.share.views.ui.common.injectable.InjectableActivity;


/**
 * @author huangyz0918 (huangyz0918@gmail.com)
 */
public class BluetoothActivity extends InjectableActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bluetooth);
    }
}
