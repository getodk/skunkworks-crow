package org.odk.share.activities;


import androidx.appcompat.widget.Toolbar;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.odk.share.R;
import org.odk.share.views.ui.hotspot.HpSenderActivity;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.LooperMode;
import org.robolectric.shadows.ShadowLog;

import static junit.framework.TestCase.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.robolectric.annotation.LooperMode.Mode.PAUSED;

@RunWith(RobolectricTestRunner.class)
@LooperMode(PAUSED)
public class HpSenderActivityTest {

    // for debugging unit tests.
    static {
        ShadowLog.stream = System.out;
    }

    private HpSenderActivity hpSenderActivity;

    @Before
    public void setUp() throws Exception {
        hpSenderActivity = Robolectric.setupActivity(HpSenderActivity.class);
    }

    /**
     * {@link Test} to assert {@link HpSenderActivity} for not null.
     */
    @Test
    public void shouldNotBeNull() {
        assertNotNull(hpSenderActivity);
    }

    /**
     * {@link Test} to assert title of {@link HpSenderActivity} for not null.
     */
    @Test
    public void titleTest() throws Exception {
        Toolbar toolbar = hpSenderActivity.findViewById(R.id.toolbar);
        assertEquals(" " + hpSenderActivity.getString(R.string.send_forms), toolbar.getTitle());
    }
}