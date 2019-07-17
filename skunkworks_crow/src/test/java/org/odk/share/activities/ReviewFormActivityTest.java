package org.odk.share.activities;

import androidx.appcompat.widget.Toolbar;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.odk.share.R;
import org.odk.share.views.ui.about.AboutActivity;
import org.odk.share.views.ui.review.ReviewFormActivity;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.shadows.ShadowLog;

import static junit.framework.TestCase.assertEquals;
import static org.junit.Assert.assertNotNull;

@RunWith(RobolectricTestRunner.class)
public class ReviewFormActivityTest {

    static {
        ShadowLog.stream = System.out;
    }

    private ReviewFormActivity reviewFormActivity;

    @Before
    public void setUp() throws Exception {
        reviewFormActivity = Robolectric.setupActivity(ReviewFormActivity.class);
    }

    /**
     * {@link Test} to assert {@link AboutActivity} for not null.
     */
    @Test
    public void shouldNotBeNull() {
        assertNotNull(reviewFormActivity);
    }

    /**
     * {@link Test} to assert title of {@link AboutActivity} for not null.
     */
    @Test
    public void titleTest() throws Exception {
        Toolbar toolbar = reviewFormActivity.findViewById(R.id.toolbar);
        assertEquals(reviewFormActivity.getString(R.string.review_form), toolbar.getTitle());
    }

    @Test
    public void acceptFormButtonTest() {

    }

    @Test
    public void rejectFormButtonTest() {

    }

    @Test
    public void reviewFormLaterButtonTest() {

    }

    @Test
    public void launchCollectTest() {

    }
}
