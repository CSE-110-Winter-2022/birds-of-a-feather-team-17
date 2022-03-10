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
public class TestHomePageFeatures {

    private AppDatabase db;
    private static int courseId = 1;
    private static int userId = 1;

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

    // adds three students to the viewAdapter, then confirms that updating the
    // priorities will affect the order
    @Test
    public void testPrioritiesChangeRecyclerViewOrder() {
        //only Ava in db right now
        Assert.assertEquals(1, db.studentsDao().getAll().size());

        //click button to start search
        ViewInteraction searchBtn = onView(
                allOf(withId(R.id.search_button),
                        isDisplayed()));
        searchBtn.perform(click());

        //go to activity to mock students
        ViewInteraction mockStuBtnView = onView(
                allOf(withId(R.id.mock_activity_btn),
                        isDisplayed()));
        mockStuBtnView.perform(click());

        //input JerryCSV text into view
        ViewInteraction csvInputView = onView(
                allOf(withId(R.id.input_csv),
                        isDisplayed()));
        csvInputView.perform(replaceText(JerryCSV));

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

        //repeat with Barry and Harry
        mockStuBtnView.perform(click());
        csvInputView.perform(replaceText(BarryCSV));
        confirmMockedStudentBtn.perform(click());
        toHomeFromCSVBtn.perform(click());

        mockStuBtnView.perform(click());
        csvInputView.perform(replaceText(HarryCSV));
        confirmMockedStudentBtn.perform(click());
        toHomeFromCSVBtn.perform(click());

        //confirm all students added to db
        Assert.assertEquals(4, db.studentsDao().getAll().size());

        //confirm that sorting by "common classes"
        ViewInteraction prioritySpinner = onView(
                allOf(withId(R.id.priority_spinner),
                        isDisplayed()));
        prioritySpinner.perform(click());

        DataInteraction optionNumber = onData(anything())
                .inAdapterView(childAtPosition(
                        withClassName(is("android.widget.PopupWindow$PopupBackgroundView")),
                        0))
                .atPosition(0);
        optionNumber.perform(click());

        //confirm that homePageViewAdapter has all 3 students
        ViewInteraction homePageRecyclerView = onView(
                allOf(withId(R.id.students_view),
                        childAtPosition(
                                withClassName(is("androidx.constraintlayout.widget.ConstraintLayout")),
                                0)));
        homePageRecyclerView.check(matches(hasChildCount(3)));

        //click into first student
        homePageRecyclerView.perform(actionOnItemAtPosition(0, click()));

        //confirm that the first is Jerry
        ViewInteraction stuDetailName = onView(
                allOf(withId(R.id.profile_name),
                        isDisplayed()));
        stuDetailName.check(matches(withText("Jerry")));

        //return to homepage
        ViewInteraction stuDetailBackBtn = onView(
                allOf(withId(R.id.button_back),
                        isDisplayed()));
        stuDetailBackBtn.perform(click());

        //now change priority to recent
        prioritySpinner.perform(click());

        DataInteraction optionRecent = onData(anything())
                .inAdapterView(childAtPosition(
                        withClassName(is("android.widget.PopupWindow$PopupBackgroundView")),
                        0))
                .atPosition(1);
        optionRecent.perform(click());

        //click into first student
        homePageRecyclerView.perform(actionOnItemAtPosition(0, click()));

        //confirm that the first is now Harry
        stuDetailName.check(matches(withText("Harry")));

        //return to homepage
        stuDetailBackBtn.perform(click());

        //now change priority to size
        prioritySpinner.perform(click());

        DataInteraction optionSize = onData(anything())
                .inAdapterView(childAtPosition(
                        withClassName(is("android.widget.PopupWindow$PopupBackgroundView")),
                        0))
                .atPosition(2);
        optionSize.perform(click());

        //click into first student
        homePageRecyclerView.perform(actionOnItemAtPosition(0, click()));

        //confirm that the first is now Barry
        stuDetailName.check(matches(withText("Barry")));
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
