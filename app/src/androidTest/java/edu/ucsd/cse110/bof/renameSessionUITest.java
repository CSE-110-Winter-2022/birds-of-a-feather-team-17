package edu.ucsd.cse110.bof;


import static androidx.test.core.app.ApplicationProvider.getApplicationContext;
import static androidx.test.espresso.Espresso.onData;
import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.closeSoftKeyboard;
import static androidx.test.espresso.action.ViewActions.pressImeActionButton;
import static androidx.test.espresso.action.ViewActions.replaceText;
import static androidx.test.espresso.action.ViewActions.scrollTo;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.contrib.RecyclerViewActions.actionOnItemAtPosition;
import static androidx.test.espresso.contrib.RecyclerViewActions.scrollToPosition;
import static androidx.test.espresso.matcher.ViewMatchers.hasChildCount;
import static androidx.test.espresso.matcher.ViewMatchers.hasDescendant;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withClassName;
import static androidx.test.espresso.matcher.ViewMatchers.withContentDescription;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withParent;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.anything;
import static org.hamcrest.Matchers.is;

import android.content.Context;
import android.provider.ContactsContract;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.AdapterView;

import androidx.lifecycle.Lifecycle;
import androidx.recyclerview.widget.RecyclerView;
import androidx.test.core.app.ActivityScenario;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.espresso.DataInteraction;
import androidx.test.espresso.ViewInteraction;
import androidx.test.espresso.matcher.BoundedMatcher;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.filters.LargeTest;
import androidx.test.rule.ActivityTestRule;
import androidx.test.runner.AndroidJUnit4;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;
import org.hamcrest.core.IsInstanceOf;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import edu.ucsd.cse110.bof.homepage.HomePageActivity;
import edu.ucsd.cse110.bof.login.NameActivity;
import edu.ucsd.cse110.bof.model.db.AppDatabase;
import edu.ucsd.cse110.bof.model.db.Course;
import edu.ucsd.cse110.bof.model.db.Student;

@LargeTest
@RunWith(AndroidJUnit4.class)
public class renameSessionUITest {

    private AppDatabase db;
    private static int courseId = 1;
    private static int userId = 1;

    private static final String AvaUUID = "6db89afd-6844-4975-bba7-a3a7d94d5003";
    private static final String someUUID1 = "a4ca50b6-941b-11ec-b909-0242ac120002";
    private static final String someUUID2 = "232dc5a5-b428-4ff0-88af-8817afc8e098";
    private static final String someUUID3 = "7299ef8f-3b21-45d3-b105-f9ceddca48bf";

    private static final String bobPhoto = "https://upload.wikimedia" +
            ".org/wikipedia/en/c/c5/Bob_the_builder.jpg";

    private static final String billCSV = someUUID1 + ",,,,\n" +
            "Bill,,,,\n" +
            "https://lh3.googleusercontent.com/pw/AM-JKLXQ2ix4dg-PzLrPOSMOOy6M3PSUrijov9jCLXs4IGSTwN73B4kr-F6Nti_4KsiUU8LzDSGPSWNKnFdKIPqCQ2dFTRbARsW76pevHPBzc51nceZDZrMPmDfAYyI4XNOnPrZarGlLLUZW9wal6j-z9uA6WQ=w854-h924-no?authuser=0,,,,\n" +
            "2021,FA,CSE,210,Tiny\n" +
            "2022,WI,CSE,110,Large\n" +
            "2022,SP,CSE,110,Gigantic\n";

    private static final String bobCSV = someUUID2 + ",,,,\n" +
            "Bob,,,,\n" +
            bobPhoto +
            "2021,FA,CSE,210,Tiny\n" +
            "2022,WI,CSE,110,Large\n" +
            "2022,SP,CSE,110,Gigantic\n";

    private static final String waveAtAvaCSV = AvaUUID + ",wave,,,\n";

    // should come first when ordering by matches since he has 2 common
    // courses with Ava
    private static final String JerryCSV = someUUID1 + ",,,,\n" +
            "Jerry,,,,\n" +
            "https://lh3.googleusercontent.com/pw/AM-JKLXQ2ix4dg-PzLrPOSMOOy6M3PSUrijov9jCLXs4IGSTwN73B4kr-F6Nti_4KsiUU8LzDSGPSWNKnFdKIPqCQ2dFTRbARsW76pevHPBzc51nceZDZrMPmDfAYyI4XNOnPrZarGlLLUZW9wal6j-z9uA6WQ=w854-h924-no?authuser=0,,,,\n" +
            "2016,FA,CSE,210,Gigantic\n" +
            "2016,WI,CSE,200,Gigantic\n";

    //should come first when ordering by size since he has a tiny common class
    private static final String BarryCSV = someUUID2 + ",,,,\n" +
            "Barry,,,,\n" +
            "https://lh3.googleusercontent.com/pw/AM-JKLXQ2ix4dg-PzLrPOSMOOy6M3PSUrijov9jCLXs4IGSTwN73B4kr-F6Nti_4KsiUU8LzDSGPSWNKnFdKIPqCQ2dFTRbARsW76pevHPBzc51nceZDZrMPmDfAYyI4XNOnPrZarGlLLUZW9wal6j-z9uA6WQ=w854-h924-no?authuser=0,,,,\n" +
            "2018,FA,CSE,99,Tiny\n";

    //should come first when ordering by recent since he has a FA22 class
    private static final String HarryCSV = someUUID3 + ",,,,\n" +
            "Harry,,,,\n" +
            "https://lh3.googleusercontent.com/pw/AM-JKLXQ2ix4dg-PzLrPOSMOOy6M3PSUrijov9jCLXs4IGSTwN73B4kr-F6Nti_4KsiUU8LzDSGPSWNKnFdKIPqCQ2dFTRbARsW76pevHPBzc51nceZDZrMPmDfAYyI4XNOnPrZarGlLLUZW9wal6j-z9uA6WQ=w854-h924-no?authuser=0,,,,\n" +
            "2022,FA,CSE,110,Large\n";


    //create Ava and her courses
    private static final Student Ava = new Student();

    //common with Jerry
    private static final Course cse210FA16G = new Course(
            courseId++,
            userId,
            2016,
            "FA",
            "CSE",
            "210",
            "Gigantic");
    //common with Jerry
    private static final Course cse200WI16G = new Course(
            courseId++,
            userId,
            2016,
            "WI",
            "CSE",
            "200",
            "Gigantic");
    //common with Barry
    private static final Course cse99FA18T = new Course(
            courseId++,
            userId,
            2018,
            "FA",
            "CSE",
            "99",
            "Tiny");
    //common with Harry
    private static final Course cse110FA22L = new Course(
            courseId++,
            userId,
            2022,
            "FA",
            "CSE",
            "110",
            "Large");
    //common with Bill and Bob
    private static final Course cse110WI22L = new Course(
            courseId++,
            userId,
            2022,
            "WI",
            "CSE",
            "110",
            "Large");

    @Rule
    public ActivityTestRule<HomePageActivity> rule =
            new ActivityTestRule<>(HomePageActivity.class, false, false);

    @Before
    public void createDb() {
        Context context = ApplicationProvider.getApplicationContext();
        db = AppDatabase.useTestSingleton(context);
        //db = AppDatabase.singleton(context);

        //Add Ava into db, then get her dbID
        Ava.setUUID(AvaUUID);
        db.studentsDao().insert(Ava);
        Ava.setStudentId(db.studentsDao().maxId());
        userId = db.studentsDao().maxId();

        //add Ava's courses to db
        db.coursesDao().insert(cse200WI16G);
        db.coursesDao().insert(cse99FA18T);
        db.coursesDao().insert(cse110WI22L);
        db.coursesDao().insert(cse210FA16G);
        db.coursesDao().insert(cse110FA22L);

        rule.launchActivity(null);
    }

    //tests mocking a student and confirms that they are added to recyclerview
    @Test
    public void testMockStudentIsAddedToRecycler() {
        //click button to start search
        ViewInteraction searchBtn = onView(
                allOf(withId(R.id.search_button),
                        isDisplayed()));
        searchBtn.perform(click());

        //click button to stop search
        searchBtn.perform(click());

        //Save session name
        ViewInteraction appCompatEditText6 = onView(
                allOf(withId(R.id.dialog_session_name),
                        childAtPosition(
                                childAtPosition(
                                        withId(android.R.id.custom),
                                        0),
                                0),
                        isDisplayed()));
        appCompatEditText6.perform(replaceText("Test"), closeSoftKeyboard());


        //confirm name button
        ViewInteraction materialButton5 = onView(
                allOf(withId(android.R.id.button1), withText("Confirm"),
                        childAtPosition(
                                childAtPosition(
                                        withClassName(is("android.widget.ScrollView")),
                                        0),
                                3)));
        materialButton5.perform(scrollTo(), click());

        //Sessions button
        ViewInteraction materialButton10 = onView(
                allOf(withId(R.id.sessions_button), withText("Sessions"),
                        childAtPosition(
                                childAtPosition(
                                        withId(android.R.id.content),
                                        0),
                                3),
                        isDisplayed()));
        materialButton10.perform(click());

        //check 1
        ViewInteraction editText2 = onView(
                allOf(withId(R.id.session_info), withText("Test"),
                        withParent(withParent(IsInstanceOf.<View>instanceOf(android.widget.FrameLayout.class))),
                        isDisplayed()));
        editText2.check(matches(withText("Test")));

        //replace text with test2
        ViewInteraction appCompatEditText7 = onView(
                allOf(withId(R.id.session_info), withText("Test"),
                        childAtPosition(
                                childAtPosition(
                                        withClassName(is("android.widget.FrameLayout")),
                                        0),
                                1),
                        isDisplayed()));
        appCompatEditText7.perform(replaceText("Test2"));

        ViewInteraction appCompatEditText8 = onView(
                allOf(withId(R.id.session_info), withText("Test2"),
                        childAtPosition(
                                childAtPosition(
                                        withClassName(is("android.widget.FrameLayout")),
                                        0),
                                1),
                        isDisplayed()));
        appCompatEditText8.perform(closeSoftKeyboard());

        //click rename

        ViewInteraction materialButton7 = onView(
                allOf(withId(R.id.rename_button), withText("Rename"),
                        childAtPosition(
                                childAtPosition(
                                        withClassName(is("android.widget.FrameLayout")),
                                        0),
                                0),
                        isDisplayed()));
        materialButton7.perform(click());

        //go back


        ViewInteraction materialButton8 = onView(
                allOf(withId(R.id.sessions_go_back_btn), withText("Go Back"),
                        childAtPosition(
                                childAtPosition(
                                        withId(android.R.id.content),
                                        0),
                                1),
                        isDisplayed()));
        materialButton8.perform(click());

        //sessions button

        ViewInteraction materialButton9 = onView(
                allOf(withId(R.id.sessions_button), withText("Sessions"),
                        childAtPosition(
                                childAtPosition(
                                        withId(android.R.id.content),
                                        0),
                                3),
                        isDisplayed()));
        materialButton9.perform(click());

        //check 2
        ViewInteraction editText3 = onView(
                allOf(withId(R.id.session_info), withText("Test2"),
                        withParent(withParent(IsInstanceOf.<View>instanceOf(android.widget.FrameLayout.class))),
                        isDisplayed()));
        editText3.check(matches(withText("Test2")));

    }



    private static Matcher<View> childAtPosition(
            final Matcher<View> parentMatcher, final int position) {

        return new TypeSafeMatcher<View>() {
            @Override
            public void describeTo(Description description) {
                description.appendText("Child at position " + position + " in parent ");
                parentMatcher.describeTo(description);
            }

            @Override
            public boolean matchesSafely(View view) {
                ViewParent parent = view.getParent();
                return parent instanceof ViewGroup && parentMatcher.matches(parent)
                        && view.equals(((ViewGroup) parent).getChildAt(position));
            }
        };
    }
}