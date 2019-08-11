package org.odk.share.activities;

import android.widget.Button;
import android.widget.EditText;

import androidx.appcompat.widget.Toolbar;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.odk.share.R;
import org.odk.share.views.ui.about.AboutActivity;
import org.odk.share.views.ui.review.ReviewFormActivity;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.shadows.ShadowActivity;
import org.robolectric.shadows.ShadowLog;
import org.robolectric.shadows.ShadowToast;

import java.util.UUID;

import static junit.framework.TestCase.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.robolectric.Shadows.shadowOf;

@RunWith(RobolectricTestRunner.class)
public class ReviewFormActivityTest {

    static {
        ShadowLog.stream = System.out;
    }

    private ReviewFormActivity reviewFormActivity;
    private ShadowActivity shadowActivity;

    @Before
    public void setUp() throws Exception {
        reviewFormActivity = Robolectric.setupActivity(ReviewFormActivity.class);
        shadowActivity = shadowOf(reviewFormActivity);
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

    /**
     * {@link Test} the accept form button.
     */
    @Test
    public void acceptFormButtonTest() {
        Button btnApprove = reviewFormActivity.findViewById(R.id.bApprove);
        btnApprove.performClick();

        //test toast.
        assertEquals(reviewFormActivity.getString(R.string.form_approved), ShadowToast.getTextOfLatestToast());

        //test if the activity is finished.
        assertTrue(shadowActivity.isFinishing());
    }

    /**
     * {@link Test} the reject {@link android.widget.Toast}.
     * {@link Test} if the {@link android.app.Activity} is finished.
     * {@link Test} if the {@link android.widget.EditText} is correct.
     */
    @Test
    public void rejectFormButtonTest() {
        Button btnRejectForm = reviewFormActivity.findViewById(R.id.bReject);
        btnRejectForm.performClick();

        //test the edit text.
        EditText feedback = reviewFormActivity.findViewById(R.id.save_feedback);
        final String testString = generateString();
        feedback.setText(testString);
        String feedbackText = feedback.getText().toString();
        assertEquals(testString, feedbackText);

        //test toast.
        assertEquals(reviewFormActivity.getString(R.string.form_rejected), ShadowToast.getTextOfLatestToast());

        //test if the activity is finished.
        assertTrue(shadowActivity.isFinishing());
    }

    /**
     * Generating a random String using {@link UUID}.
     */
    private String generateString() {
        String uuid = UUID.randomUUID().toString();
        return "uuid = " + uuid;
    }

    /**
     * {@link Test} if the {@link android.app.Activity} is finished.
     */
    @Test
    public void reviewFormLaterButtonTest() {
        Button btnReviewLater = reviewFormActivity.findViewById(R.id.bReviewLater);
        btnReviewLater.performClick();
        assertTrue(shadowActivity.isFinishing());
    }
}