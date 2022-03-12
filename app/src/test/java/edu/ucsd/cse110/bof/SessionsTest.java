package edu.ucsd.cse110.bof;

import android.content.Context;

import androidx.lifecycle.Lifecycle;
import androidx.test.core.app.ActivityScenario;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.ArrayList;
import java.util.List;

import edu.ucsd.cse110.bof.homepage.HomePageActivity;
import edu.ucsd.cse110.bof.model.StudentWithCourses;
import edu.ucsd.cse110.bof.model.db.AppDatabase;
import edu.ucsd.cse110.bof.model.db.Course;
import edu.ucsd.cse110.bof.model.db.Session;
import edu.ucsd.cse110.bof.model.db.Student;

@RunWith(AndroidJUnit4.class)
public class SessionsTest {

    private AppDatabase db;
    private static int courseId = 1;
    private static int userId = 1;

    ActivityScenario<HomePageActivity> scenario;

    private static final String someUUID = "a4ca50b6-941b-11ec-b909-0242ac120002";

    private static final String bobPhoto = "https://upload.wikimedia" +
            ".org/wikipedia/en/c/c5/Bob_the_builder.jpg";

    //create Ava's courses
    private static final Course cse100FA22S = new Course(
            courseId++,
            userId,
            2022,
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
    private static final Course cse210FA21S = new Course(    //this should not appear in the list
            courseId++,
            -1,
            2021,
            "FA",
            "CSE",
            "210",
            "Small");

    @Before
    public void createDatabaseAndUser() {
        //Create db
        Context context = ApplicationProvider.getApplicationContext();
        db = AppDatabase.useTestSingleton(context);
        //db = AppDatabase.singleton(context);

        //create Ava (user) and insert her into db, then get her dbID
        Student Ava = new Student();
        db.studentsDao().insert(Ava);
        Ava.setStudentId(db.studentsDao().maxId());

        //add Ava's courses to db
        db.coursesDao().insert(cse100FA22S);
        db.coursesDao().insert(cse110WI22L);
    }

    // creates one empty session and one that finds Bob
    @Test
    public void createEmptyAndNonEmptySessions() {
        ActivityScenario<HomePageActivity> scenario =
                ActivityScenario.launch(HomePageActivity.class);

        //make Bob and his courses to mock:
        Student Bob = new Student("Bob", bobPhoto, someUUID);

        List<Course> bobCourses = new ArrayList<>();
        bobCourses.add(cse110WI22L);
        bobCourses.add(cse210FA21S);

        StudentWithCourses BobAndCourses = new StudentWithCourses(Bob,
                bobCourses, "");

        //move to CREATED to make necessary objects
        scenario.moveToState(Lifecycle.State.CREATED);

        scenario.onActivity( activity -> {
            //use test db
            activity.setDb(db);

            //click start and then stop to create a session: (empty)
            activity.onStartSearchingClicked();
            activity.onStopSearchingClicked();

            // now mock Bob and start searching again to create another
            // session: (only Bob)
            activity.setMockedStudent(BobAndCourses);
            activity.onStartSearchingClicked();
            activity.onStopSearchingClicked();

            //get Bob's database ID
            BobAndCourses.getStudent().setStudentId(db.studentsDao().maxId());

            // confirm that there are 2 sessions
            List<Session> sessions = db.sessionsDao().getAll();
            Assert.assertEquals(2, sessions.size());

            //the first session found no one
            List<Integer> firstExpectedList = new ArrayList<>();

            Assert.assertEquals(firstExpectedList, sessions.get(0).getStudentList());

            //the second session found Bob
            List<Integer> secondExpectedList = new ArrayList<>();
            secondExpectedList.add(BobAndCourses.getStudent().getStudentId());

            Assert.assertEquals(secondExpectedList, sessions.get(1).getStudentList());
        });
    }

    // tests that two sessions do not overlap if the first is stopped
    @Test
    public void createTwoNonEmptySessions() {
        ActivityScenario<HomePageActivity> scenario =
                ActivityScenario.launch(HomePageActivity.class);

        //make Bob and his courses to mock:
        Student Bob = new Student("Bob", bobPhoto, someUUID);

        List<Course> bobCourses = new ArrayList<>();
        bobCourses.add(cse110WI22L);
        bobCourses.add(cse210FA21S);

        StudentWithCourses BobAndCourses = new StudentWithCourses(Bob, bobCourses, "");


        //make Carrie and her courses to mock:
        Student Carrie = new Student("Carrie", bobPhoto, someUUID);

        List<Course> carrieCourses = new ArrayList<>();
        carrieCourses.add(cse110WI22L);
        carrieCourses.add(cse210FA21S);

        StudentWithCourses CarrieAndCourses = new StudentWithCourses(Carrie,
                carrieCourses, "");

        //move to CREATED to make necessary objects
        scenario.moveToState(Lifecycle.State.CREATED);

        scenario.onActivity( activity -> {
            //use test db
            activity.setDb(db);

            // Mock Bob and start searching to create a session: (only Bob)
            activity.setMockedStudent(BobAndCourses);
            activity.onStartSearchingClicked();
            activity.onStopSearchingClicked();

            //get Bob's database ID
            BobAndCourses.getStudent().setStudentId(db.studentsDao().maxId());
        });

        scenario.recreate();
        scenario.moveToState(Lifecycle.State.CREATED);

        scenario.onActivity(activity -> {
            //use test db
            activity.setDb(db);

            // now mock Carrie and start searching again to create another
            // session: (only Carrie)
            activity.setMockedStudent(CarrieAndCourses);
            activity.onStartSearchingClicked();
            activity.onStopSearchingClicked();

            //get Carrie's database ID
            CarrieAndCourses.getStudent().setStudentId(db.studentsDao().maxId());

            List<Student> students = db.studentsDao().getAll();

            // confirm that there are 2 sessions
            List<Session> sessions = db.sessionsDao().getAll();
            Assert.assertEquals(2, sessions.size());

            //the first session found only Bob
            List<Integer> firstExpectedList = new ArrayList<>();
            firstExpectedList.add(BobAndCourses.getStudent().getStudentId());


            Assert.assertEquals(firstExpectedList, sessions.get(0).getStudentList());

            //the second session found only Carrie
            List<Integer> secondExpectedList = new ArrayList<>();
            secondExpectedList.add(CarrieAndCourses.getStudent().getStudentId());

            Assert.assertEquals(secondExpectedList, sessions.get(1).getStudentList());
        });
    }

    @Test
    public void createAndRenameSession() {
        ActivityScenario<HomePageActivity> scenario =
                ActivityScenario.launch(HomePageActivity.class);

        //make Bob and his courses to mock:
        Student Bob = new Student("Bob", bobPhoto, someUUID);

        List<Course> bobCourses = new ArrayList<>();
        bobCourses.add(cse110WI22L);
        bobCourses.add(cse210FA21S);

        StudentWithCourses BobAndCourses = new StudentWithCourses(Bob, bobCourses, "");


        //make Carrie and her courses to mock:
        Student Carrie = new Student("Carrie", bobPhoto, someUUID);

        List<Course> carrieCourses = new ArrayList<>();
        carrieCourses.add(cse110WI22L);
        carrieCourses.add(cse210FA21S);

        StudentWithCourses CarrieAndCourses = new StudentWithCourses(Carrie,
                carrieCourses, "");

        //move to CREATED to make necessary objects
        scenario.moveToState(Lifecycle.State.CREATED);

        scenario.onActivity( activity -> {
            //use test db
            activity.setDb(db);

            // Mock Bob and start searching to create a session: (only Bob)
            activity.setMockedStudent(BobAndCourses);
            activity.onStartSearchingClicked();
            activity.onStopSearchingClicked();

            //get Bob's database ID
            BobAndCourses.getStudent().setStudentId(db.studentsDao().maxId());
        });

        scenario.recreate();
        scenario.moveToState(Lifecycle.State.CREATED);

        scenario.onActivity(activity -> {
            //use test db
            activity.setDb(db);

            // now mock Carrie and start searching again to create another
            // session: (only Carrie)
            activity.setMockedStudent(CarrieAndCourses);
            activity.onStartSearchingClicked();
            activity.onStopSearchingClicked();

            //get Carrie's database ID
            CarrieAndCourses.getStudent().setStudentId(db.studentsDao().maxId());

            List<Student> students = db.studentsDao().getAll();

            // confirm that there are 2 sessions
            List<Session> sessions = db.sessionsDao().getAll();
            Assert.assertEquals(2, sessions.size());

            //the first session found only Bob
            List<Integer> firstExpectedList = new ArrayList<>();
            firstExpectedList.add(BobAndCourses.getStudent().getStudentId());


            Assert.assertEquals(firstExpectedList, sessions.get(0).getStudentList());

            //the second session found only Carrie
            List<Integer> secondExpectedList = new ArrayList<>();
            secondExpectedList.add(CarrieAndCourses.getStudent().getStudentId());

            Assert.assertEquals(secondExpectedList, sessions.get(1).getStudentList());
        });
    }
}
