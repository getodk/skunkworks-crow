package org.odk.share.activities;

import android.widget.Button;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.odk.share.R;
import org.odk.share.views.ui.receive.ScannerActivity;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.Shadows;
import org.robolectric.shadows.ShadowLog;

import static junit.framework.TestCase.assertTrue;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertNotNull;

/**
 * Created by Chromicle on 21/8/19.
 */
@RunWith(RobolectricTestRunner.class)
public class ScannerActivtyTest {

    // for debugging unit tests.
    static {
        ShadowLog.stream = System.out;
    }

    private ScannerActivity scannerActivity;

    @Before
    public void setUp() throws Exception {
        scannerActivity = Robolectric.setupActivity(ScannerActivity.class);
    }


    /**
     * {@link Test} to assert {@link ScannerActivity} for not null.
     */
    @Test
    public void shouldNotBeNull() {
        assertNotNull(scannerActivity);
    }


    /**
     * {@link Test} to assert flashButton's functioning.
     */
    @Test
    public void flashLightButtonTest() throws Exception {
        Button flashButton = scannerActivity.findViewById(R.id.switch_flashlight);
        int flashOff = Shadows.shadowOf(flashButton.getBackground()).getCreatedFromResId();
        assertThat("flash light button", R.drawable.ic_flash_white_off, is(equalTo(flashOff)));
        assertTrue(flashButton.performClick());
        int flashOn = Shadows.shadowOf(flashButton.getBackground()).getCreatedFromResId();
        assertThat("flash button on", R.drawable.ic_flash_white_on, is(equalTo(flashOn)));
    }


}
