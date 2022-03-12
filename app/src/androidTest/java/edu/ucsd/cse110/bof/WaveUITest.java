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

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.rules.ActivityScenarioRule;

import com.google.android.material.bottomappbar.BottomAppBar;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;

import edu.ucsd.cse110.bof.model.db.AppDatabase;
import edu.ucsd.cse110.bof.model.db.Course;
import edu.ucsd.cse110.bof.model.db.Student;
import edu.ucsd.cse110.bof.viewProfile.StudentDetailActivity;

/* Given Olivia appears on Ava’s BoFs search screen
 * When Ava taps the hollow wave icon in Olivia's student profile
 * Then the hand icon should become solid
 */
public class WaveUITest {

    private static int courseId = 1;
    private static int userId = 1;
    private static final String someUUID = "a4ca50b6-941b-11ec-b909-0242ac120002";

    private static final String bobPhoto = "https://upload.wikimedia" +
            ".org/wikipedia/en/c/c5/Bob_the_builder.jpg";

    private static final Course cse100FA22S_Ava = new Course(
            courseId++,
            userId,
            2022,
            "FA",
            "CSE",
            "100",
            "Small");
    private static final Course cse110WI22L_Ava = new Course(
            courseId++,
            userId,
            2022,
            "WI",
            "CSE",
            "110",
            "Large");

    private static final Course cse110WI22L_Bob = new Course(
            courseId++,
            2,
            2022,
            "WI",
            "CSE",
            "110",
            "Large");
    private static final Course cse210FA21S_Bob = new Course(
            courseId++,
            2,
            2021,
            "FA",
            "CSE",
            "210",
            "Small");

    private ActivityScenarioRule<StudentDetailActivity> activityScenarioRule;

    @Rule
    public ActivityScenarioRule databaseAndASR() {
        Context context = ApplicationProvider.getApplicationContext();
        AppDatabase db = AppDatabase.useTestSingleton(context);

        //create Ava (user) and insert her into db, then get her dbID
        Student Ava = new Student();
        db.studentsDao().insert(Ava);
        Ava.setStudentId(db.studentsDao().maxId());

        //add Ava's courses to db
        db.coursesDao().insert(cse100FA22S_Ava);
        db.coursesDao().insert(cse110WI22L_Ava);

        //insert Bob and his courses into db:
        Student Bob = new Student("Bob", bobPhoto, someUUID);
        db.studentsDao().insert(Bob);

        //add Bob's courses to db
        db.coursesDao().insert(cse110WI22L_Bob);
        db.coursesDao().insert(cse210FA21S_Bob);

        Intent intent = new Intent(context, StudentDetailActivity.class);
        intent.putExtra("student_id", 2);

        return activityScenarioRule = new ActivityScenarioRule<>(intent);
    }

    @Test
    public void waveShown() {
        String waveOn = getApplicationContext().getString(R.string.wave_on);

        //Simulate wave button click
        onView(withId(R.id.wave_icon))
                .perform(click());

        //Check if wave icon has changed
        onView(withId(R.id.wave_icon))
                .check(matches(withContentDescription(waveOn)));
    }
}
