package org.odk.share.activities;


import androidx.appcompat.widget.Toolbar;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.odk.share.R;
import org.odk.share.views.ui.bluetooth.BtReceiverActivity;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;

import static org.junit.Assert.assertNotNull;

@RunWith(RobolectricTestRunner.class)
public class BtReceiverActivityTest {

    private BtReceiverActivity btReceiverActivity;

    @Before
    public void setUp() throws Exception {
        btReceiverActivity = Robolectric.setupActivity(BtReceiverActivity.class);
    }

    /**
     * {@link Test} to assert {@link BtReceiverActivity} for not null.
     */
    @Test
    public void shouldNotBeNull() {
        assertNotNull(btReceiverActivity);
    }

    /**
     * {@link Test} to assert title of {@link BtReceiverActivity} for not null.
     */
    @Test
    public void titleTest() throws Exception {
        Toolbar toolbar = btReceiverActivity.findViewById(R.id.toolbar);
        Assert.assertEquals(btReceiverActivity.getString(R.string.connect_bluetooth_title), toolbar.getTitle());
    }
}
