package org.odk.share.activities;


import androidx.appcompat.widget.Toolbar;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.odk.share.R;
import org.odk.share.views.ui.hotspot.HpReceiverActivity;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.LooperMode;
import org.robolectric.shadows.ShadowLog;

import static junit.framework.TestCase.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.robolectric.annotation.LooperMode.Mode.PAUSED;

@RunWith(RobolectricTestRunner.class)
@LooperMode(PAUSED)
public class HpReceiverActivityTest {

    // for debugging unit tests.
    static {
        ShadowLog.stream = System.out;
    }

    private HpReceiverActivity hpReceiverActivity;

    @Before
    public void setUp() throws Exception {
        hpReceiverActivity = Robolectric.setupActivity(HpReceiverActivity.class);
    }

    /**
     * {@link Test} to assert {@link HpReceiverActivity} for not null.
     */
    @Test
    public void shouldNotBeNull() {
        assertNotNull(hpReceiverActivity);
    }

    /**
     * {@link Test} to assert title of {@link HpReceiverActivity} for not null.
     */
    @Test
    public void titleTest() throws Exception {
        Toolbar toolbar = hpReceiverActivity.findViewById(R.id.toolbar);
        assertEquals(" " + hpReceiverActivity.getString(R.string.connect_wifi), toolbar.getTitle());
    }
}