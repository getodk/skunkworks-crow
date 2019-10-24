package org.odk.share.activities;

import androidx.appcompat.widget.Toolbar;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.odk.share.R;
import org.odk.share.views.ui.send.SendFormsActivity;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.LooperMode;
import org.robolectric.shadows.ShadowLog;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.robolectric.annotation.LooperMode.Mode.PAUSED;

@RunWith(RobolectricTestRunner.class)
@LooperMode(PAUSED)
public class SendFormsActivityTest {

    static {
        ShadowLog.stream = System.out;
    }

    private SendFormsActivity sendFormsActivity;

    @Before
    public void setUp() throws Exception {
        sendFormsActivity = Robolectric.setupActivity(SendFormsActivity.class);
    }

    /**
     * {@link Test} to assert {@link SendFormsActivity} for not null.
     */
    @Test
    public void shouldNotBeNull() {
        assertNotNull(sendFormsActivity);
    }

    /**
     * {@link Test} to assert title of {@link SendFormsActivity} for not null.
     */
    @Test
    public void titleTest() throws Exception {
        Toolbar toolbar = sendFormsActivity.findViewById(R.id.toolbar);
        assertEquals(sendFormsActivity.getString(R.string.app_name), toolbar.getTitle());
    }
}
