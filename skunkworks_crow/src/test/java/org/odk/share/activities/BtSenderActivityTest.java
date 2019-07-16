package org.odk.share.activities;

import androidx.appcompat.widget.Toolbar;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.odk.share.R;
import org.odk.share.views.ui.bluetooth.BtSenderActivity;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.LooperMode;
import org.robolectric.shadows.ShadowLog;

import static org.junit.Assert.assertEquals;
import static org.robolectric.annotation.LooperMode.Mode.PAUSED;

@RunWith(RobolectricTestRunner.class)
@LooperMode(PAUSED)
public class BtSenderActivityTest {

    static {
        ShadowLog.stream = System.out;
    }

    private BtSenderActivity btSenderActivity;

    @Before
    public void setUp() throws Exception {
        btSenderActivity = Robolectric.setupActivity(BtSenderActivity.class);
    }

    /**
     * {@link Test} to assert title of {@link BtSenderActivity} for not null.
     */
    @Test
    public void titleTest() throws Exception {
        Toolbar toolbar = btSenderActivity.findViewById(R.id.toolbar);
        assertEquals(btSenderActivity.getString(R.string.send_instance_title), toolbar.getTitle());
    }
}
