package edu.ucsd.cse110.bof;


import static androidx.test.espresso.Espresso.onData;
import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.replaceText;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.contrib.RecyclerViewActions.actionOnItemAtPosition;
import static androidx.test.espresso.contrib.RecyclerViewActions.scrollToPosition;
import static androidx.test.espresso.matcher.ViewMatchers.hasChildCount;
import static androidx.test.espresso.matcher.ViewMatchers.hasDescendant;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withClassName;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withParent;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.allOf;
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
public class TestHomePageFeatures {

    private AppDatabase db;
    private static int courseId = 1;
    private static int userId = 1;

    private static final String bobPhoto = "https://upload.wikimedia" +
            ".org/wikipedia/en/c/c5/Bob_the_builder.jpg";

    private static final String billCSV = "Bill,,,,\n" +
            "https://lh3.googleusercontent.com/pw/AM-JKLXQ2ix4dg-PzLrPOSMOOy6M3PSUrijov9jCLXs4IGSTwN73B4kr-F6Nti_4KsiUU8LzDSGPSWNKnFdKIPqCQ2dFTRbARsW76pevHPBzc51nceZDZrMPmDfAYyI4XNOnPrZarGlLLUZW9wal6j-z9uA6WQ=w854-h924-no?authuser=0,,,,\n" +
            "2021,FA,CSE,210,Tiny\n" +
            "2022,WI,CSE,110,Large\n" +
            "2022,SP,CSE,110,Gigantic\n";

    private static final String bobCSV = "Bob,,,,\n" +
            bobPhoto +
            "2021,FA,CSE,210,Tiny\n" +
            "2022,WI,CSE,110,Large\n" +
            "2022,SP,CSE,110,Gigantic\n";


    private static final Student Ava = new Student();
    //create Ava's courses
    private static final Course cse12FA21S = new Course(
            courseId++,
            userId,
            2021,
            "FA",
            "CSE",
            "12",
            "Small");
    private static final Course cse100FA21S = new Course(
            courseId++,
            userId,
            2021,
            "FA",
            "CSE",
            "100",
            "Small");
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
        db.studentsDao().insert(Ava);
        Ava.setStudentId(db.studentsDao().maxId());
        userId = db.studentsDao().maxId();

        //add Ava's courses to db
        db.coursesDao().insert(cse12FA21S);
        db.coursesDao().insert(cse100FA21S);
        db.coursesDao().insert(cse110WI22L);

        rule.launchActivity(null);
    }

    @Test
    public void testMockStudentIsAddedToRecycler() {
        Assert.assertEquals(1, db.studentsDao().getAll().size());

        //go to activity to mock students
        ViewInteraction mockStuBtnView = onView(
                allOf(withId(R.id.mock_activity_btn),
                        isDisplayed()));
        mockStuBtnView.perform(click());


        //input csv text into view
        ViewInteraction csvInputView = onView(
                allOf(withId(R.id.input_csv),
                        isDisplayed()));
        csvInputView.perform(replaceText(billCSV));

        //click confirm
        ViewInteraction confirmMockedStudentView = onView(
                allOf(withId(R.id.confirmButton),
                        isDisplayed()));
        confirmMockedStudentView.perform(click());

        //go back to homepage:
        ViewInteraction toHomePageBtn = onView(
                allOf(withId(R.id.button2),
                        isDisplayed()));
        toHomePageBtn.perform(click());

        //click button to start search
        ViewInteraction searchBtn = onView(
                allOf(withId(R.id.search_button),
                        isDisplayed()));
        searchBtn.perform(click());

        //Bill added to db correctly
        Assert.assertEquals(2, db.studentsDao().getAll().size());

        //bill should appear and be clickable
        ViewInteraction recyclerView = onView(
                allOf(withId(R.id.students_view),
                        childAtPosition(
                                withClassName(is("androidx.constraintlayout.widget.ConstraintLayout")),
                                0)));
        recyclerView.perform(actionOnItemAtPosition(0, click()));

        //in profile view, the name should be Bill
        ViewInteraction detailNameView = onView(
                allOf(withId(R.id.profile_name),
                        isDisplayed()));
        detailNameView.check(matches(withText("Bill")));

        //Bill has only one common class
        onView(withId(R.id.list_classes_recycler)).check(matches(hasChildCount(1)));

    }

    //creates two students, Bill and Bob, and adds them to separate sessions
    @Test
    public void testSessionsWithDifferentStudents() {
        Assert.assertEquals(1, db.studentsDao().getAll().size());

        //go to activity to mock students
        ViewInteraction mockStuBtnView = onView(
                allOf(withId(R.id.mock_activity_btn),
                        isDisplayed()));
        mockStuBtnView.perform(click());

        //input csv text into view
        ViewInteraction csvInputView = onView(
                allOf(withId(R.id.input_csv),
                        isDisplayed()));
        csvInputView.perform(replaceText(billCSV));

        //click confirm
        ViewInteraction confirmMockedStudentBtn = onView(
                allOf(withId(R.id.confirmButton),
                        isDisplayed()));
        confirmMockedStudentBtn.perform(click());

        //go back to homepage:
        ViewInteraction toHomeFromCSVBtn = onView(
                allOf(withId(R.id.button2),
                        isDisplayed()));
        toHomeFromCSVBtn.perform(click());

        //click button to start search
        ViewInteraction searchBtn = onView(
                allOf(withId(R.id.search_button),
                        isDisplayed()));
        searchBtn.perform(click());

        //Bill added to db correctly
        Assert.assertEquals(2, db.studentsDao().getAll().size());

        //bill should be the only item in the viewAdapter
        ViewInteraction homePageRecyclerView = onView(
                allOf(withId(R.id.students_view),
                        childAtPosition(
                                withClassName(is("androidx.constraintlayout.widget.ConstraintLayout")),
                                0)));
        homePageRecyclerView.check(matches(hasChildCount(1)));

        //go to bill's detail page
        homePageRecyclerView.perform(actionOnItemAtPosition(0, click()));

        //in profile view, the name should be Bill
        ViewInteraction billDetailNameView = onView(
                allOf(withId(R.id.profile_name),
                        isDisplayed()));
        billDetailNameView.check(matches(withText("Bill")));

        //return to homepage
        ViewInteraction stuDetailBackBtn = onView(
                allOf(withId(R.id.button_back),
                        isDisplayed()));
        stuDetailBackBtn.perform(click());

        //stop the session
        searchBtn.perform(click());

        //go back to nearby mock message activity, use bobCSV instead
        mockStuBtnView.perform(click());
        csvInputView.perform(replaceText(bobCSV));
        confirmMockedStudentBtn.perform(click());
        toHomeFromCSVBtn.perform(click());

        //start new search session
        searchBtn.perform(click());

        //Bob should be the only item in the viewAdapter
        homePageRecyclerView.check(matches(hasChildCount(1)));

        //go to Bob's detail page
        homePageRecyclerView.perform(actionOnItemAtPosition(0, click()));

        //in profile view, the name should be Bob
        ViewInteraction bobDetailNameView = onView(
                allOf(withId(R.id.profile_name),
                        isDisplayed()));
        bobDetailNameView.check(matches(withText("Bob")));

        //return to home
        stuDetailBackBtn.perform(click());

        //stop the session
        searchBtn.perform(click());

        //go to sessions page
        ViewInteraction toSessionsBtn = onView(
                allOf(withId(R.id.sessions_button),
                        isDisplayed()));
        toSessionsBtn.perform(click());

        //check that there are two sessions
        ViewInteraction sessionsRecyclerView = onView(
                allOf(withId(R.id.sessions_view),
                        childAtPosition(
                                withClassName(is("androidx.constraintlayout.widget.ConstraintLayout")),
                                0)));
        sessionsRecyclerView.check(matches(hasChildCount(2)));

        //click into first session and confirm that it is only Bill
        sessionsRecyclerView.perform(actionOnItemAtPosition(0, click()));

        // bill should be the only item in the viewAdapter, confirm by
        // clicking in
        ViewInteraction historyViewAdapter = onView(
                allOf(withId(R.id.history_view),
                        childAtPosition(
                                withClassName(is("androidx.constraintlayout.widget.ConstraintLayout")),
                                0)));

        //confirm that there was only one student found this session
        historyViewAdapter.check(matches(hasChildCount(1)));

        historyViewAdapter.perform(actionOnItemAtPosition(0, click()));

        //in profile view, the name should be Bill
        billDetailNameView.check(matches(isDisplayed()));
        billDetailNameView.check(matches(withText("Bill")));

        //leave profile page
        stuDetailBackBtn.perform(click());

        //leave session detail page
        ViewInteraction sessionDetailGoBackBtn = onView(
                allOf(withId(R.id.go_back_btn),
                        isDisplayed()));
        sessionDetailGoBackBtn.perform(click());

        //click into second session to confirm that it is only Bob
        sessionsRecyclerView.perform(actionOnItemAtPosition(1, click()));

        //click into Bob's profile
        historyViewAdapter.check(matches(isDisplayed()));
        historyViewAdapter.perform(actionOnItemAtPosition(0, click()));

        //in profile view, the name should be Bob
        bobDetailNameView.check(matches(isDisplayed()));
        bobDetailNameView.check(matches(withText("Bob")));
    }

    /*
    public static Matcher<View> withIndex(final Matcher<View> matcher, final int index) {
        return new BoundedMatcher<View, RecyclerView>(RecyclerView.class) {
            int currentIndex = 0;

            @Override
            public void describeTo(Description description) {
                description.appendText("with index: ");
                description.appendValue(index);
                matcher.describeTo(description);
            }

            @Override
            public boolean matchesSafely(final RecyclerView view) {
                RecyclerView.ViewHolder viewHolder =
                        view.findViewHolderForAdapterPosition(index);
                if (viewHolder == null) {
                    // has no item on such position
                    return false;
                }
                return matcher.matches(viewHolder.itemView);
            }
        };
    }
    */
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
