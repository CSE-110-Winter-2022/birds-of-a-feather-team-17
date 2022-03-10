package edu.ucsd.cse110.bof;

import static androidx.test.core.app.ApplicationProvider.getApplicationContext;
import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.RootMatchers.withDecorView;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withContentDescription;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;

import static org.hamcrest.Matchers.not;

import android.view.View;

import androidx.test.core.app.ActivityScenario;
import androidx.test.ext.junit.rules.ActivityScenarioRule;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import edu.ucsd.cse110.bof.viewProfile.StudentDetailActivity;

/* Given Olivia appears on Ava’s BoFs search screen
 * When Ava taps the hollow wave icon in Olivia's student profile
 * Then the hand icon should become solid
 * And a toast should display saying “Wave sent!”
 */

public class WaveUITest {

    private View decorView;

    @Rule
    public ActivityScenarioRule<StudentDetailActivity> activityScenarioRule =
            new ActivityScenarioRule<>(StudentDetailActivity.class);

    @Before
    public void setup() {
        activityScenarioRule.getScenario().onActivity(activity -> decorView = activity.getWindow().getDecorView());
    }

    @Test
    public void waveAndToastShown() {
        String waveToast = getApplicationContext().getString(R.string.wave_sent);
        String waveOn = getApplicationContext().getString(R.string.wave_on);

        //Simulate wave button click
        onView(withId(R.id.imageButton))
                .perform(click());

        //Check if toast is displayed with "Wave sent!"
        onView(withText(waveToast))
                .inRoot(withDecorView(not(decorView)))
                .check(matches(isDisplayed()));

        //Check if wave icon has changed
        onView(withId(R.id.imageButton))
                .check(matches(withContentDescription(waveOn)));
    }
}
