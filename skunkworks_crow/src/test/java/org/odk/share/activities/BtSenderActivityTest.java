package org.odk.share.activities;


import androidx.appcompat.widget.Toolbar;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.odk.share.R;
import org.odk.share.views.ui.bluetooth.BtSenderActivity;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;

@RunWith(RobolectricTestRunner.class)
public class BtSenderActivityTest {

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
        Assert.assertEquals(btSenderActivity.getString(R.string.send_instance_title), toolbar.getTitle());
    }
}
