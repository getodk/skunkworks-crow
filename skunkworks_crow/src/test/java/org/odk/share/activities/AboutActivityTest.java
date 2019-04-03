package org.odk.share.activities;


import android.content.Intent;
import android.view.View;

import androidx.appcompat.widget.Toolbar;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.odk.share.R;
import org.odk.share.views.ui.about.AboutActivity;
import org.odk.share.views.ui.about.AboutAdapter;
import org.odk.share.views.ui.about.AboutItem;
import org.odk.share.views.ui.about.WebViewActivity;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.LooperMode;
import org.robolectric.shadows.ShadowActivity;
import org.robolectric.shadows.ShadowIntent;
import org.robolectric.shadows.ShadowLog;

import java.util.ArrayList;
import java.util.List;

import static junit.framework.TestCase.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.robolectric.Shadows.shadowOf;
import static org.robolectric.annotation.LooperMode.Mode.PAUSED;

@RunWith(RobolectricTestRunner.class)
@LooperMode(PAUSED)
public class AboutActivityTest {

    // for debugging unit tests.
    static {
        ShadowLog.stream = System.out;
    }

    private AboutActivity aboutActivity;
    private List<AboutItem> aboutItems;

    @Before
    public void setUp() throws Exception {
        aboutItems = new ArrayList<>();
        aboutActivity = Robolectric.setupActivity(AboutActivity.class);
    }

    /**
     * {@link Test} to assert {@link AboutActivity} for not null.
     */
    @Test
    public void shouldNotBeNull() {
        assertNotNull(aboutActivity);
    }

    /**
     * {@link Test} to assert title of {@link AboutActivity} for not null.
     */
    @Test
    public void titleTest() throws Exception {
        Toolbar toolbar = aboutActivity.findViewById(R.id.toolbar);
        assertEquals(aboutActivity.getString(R.string.about), toolbar.getTitle());
    }

    /**
     * {@link Test} the {@link AboutAdapter} for the correct item account.
     */
    @Test
    public void aboutAdapterTest() {
        //test the click event of about items.
        ShadowActivity shadowActivity = shadowOf(aboutActivity);
        AboutAdapter adapter = new AboutAdapter(aboutActivity, (View v, int position) -> {
            {
                Intent startedIntent = shadowActivity.getNextStartedActivity();
                ShadowIntent shadowIntent = shadowOf(startedIntent);
                assertEquals(WebViewActivity.class.getName(),
                        shadowIntent.getIntentClass().getName());
                assertEquals("file:///android_asset/open_source_licenses.html",
                        startedIntent.getStringExtra("url"));
            }
        });

        //test the item operations with about adapter.
        assertEquals(0, adapter.getItemCount());

        aboutItems.add(new AboutItem(R.string.open_source_licenses, R.drawable.ic_stars));
        aboutItems.add(new AboutItem(R.string.open_source_licenses, R.drawable.ic_stars));
        aboutItems.add(new AboutItem(R.string.open_source_licenses, R.drawable.ic_stars));
        adapter.setItems(aboutItems);

        assertEquals(3, adapter.getItemCount());
    }
}
