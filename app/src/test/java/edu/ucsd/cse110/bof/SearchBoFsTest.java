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

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;

import edu.ucsd.cse110.bof.homepage.HomePageActivity;
import edu.ucsd.cse110.bof.model.db.AppDatabase;
import edu.ucsd.cse110.bof.model.db.Course;
import edu.ucsd.cse110.bof.model.db.Student;

/**
 * Tests mocking a student and receiving that student on the homepage
 */
@RunWith(AndroidJUnit4.class)
public class SearchBoFsTest {

    private AppDatabase db;
    private static int courseId = 1;

    private static final String bobPhoto = "https://upload.wikimedia" +
            ".org/wikipedia/en/c/c5/Bob_the_builder.jpg";

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

        //create Ava's courses
        Course cse100FA22S = new Course(
                courseId++,
                Ava.getStudentId(),
                2022,
                "FA",
                "CSE",
                "100",
                "Small");
        Course cse110WI22L = new Course(
                courseId++,
                Ava.getStudentId(),
                2022,
                "WI",
                "CSE",
                "110",
                "Large");

        //add Ava's courses to db
        db.coursesDao().insert(cse100FA22S);
        db.coursesDao().insert(cse110WI22L);
    }

    //Basic test: Bob should be added to both db and viewAdapter
    @Test
    public void bobIsAddedWithOnlyOneCommonClass() {
        ActivityScenario<HomePageActivity> scenario =
                ActivityScenario.launch(HomePageActivity.class);

        //make Bob and his courses to mock:
        Student Bob = new Student("Bob", bobPhoto, "ffc910fd-e52f-4829-a649-5f2c44a9fce4");

        Course cse110WI22L = new Course(    //this should appear in the list
                courseId++,
                -1,
                2022,
                "WI",
                "CSE",
                "110",
                "Large");
        Course cse210FA21S = new Course(    //this should not appear in the list
                courseId++,
                -1,
                2021,
                "FA",
                "CSE",
                "210",
                "Small");

        List<Course> bobCourses = new ArrayList<>();
        bobCourses.add(cse110WI22L);
        bobCourses.add(cse210FA21S);

        StudentWithCourses BobAndCourses = new StudentWithCourses(Bob, bobCourses,"");

        //move to CREATED to make necessary objects
        scenario.moveToState(Lifecycle.State.CREATED);

        scenario.onActivity( activity -> {
            //use test db and automatically mock Bill without going to NMM
            activity.setMockedStudent(BobAndCourses);
            activity.setDb(db);

            //get reference to list of students in homepage viewadapter:
            ArrayList<Student> viewAdapterList =
                    (ArrayList<Student>) activity.getStudentsViewAdapter().getStudents();

            //emulate clicking start search to start the FakedMessageListener;
            activity.onStartSearchingClicked();

            //Bob should now be visible in viewAdapterList
            Assert.assertEquals(Bob, viewAdapterList.get(0));
            Assert.assertEquals(1, viewAdapterList.size());

            Student a = db.studentsDao().get(2);
            Student b = db.studentsDao().get(1);

            //Bob should also have been inserted into db as second student
            Assert.assertEquals(Bob, db.studentsDao().get(2));

            //Bob should only have one course in the list/database
            List<Course> bobExpectedCourses = new ArrayList<>();
            bobExpectedCourses.add(cse110WI22L);

            Assert.assertEquals(bobExpectedCourses,
                    db.coursesDao().getForStudent(2));
        });
    }

    // testing that identical student is only inserted once into db, but
    // always inserted into viewAdapter
    @Test
    public void bobIsAlreadyInDatabase() {
        ActivityScenario<HomePageActivity> scenario =
                ActivityScenario.launch(HomePageActivity.class);

        //move to CREATED to make necessary objects
        scenario.moveToState(Lifecycle.State.CREATED);

        //make Bob and his courses to mock:
        Student Bob = new Student("Bob", bobPhoto, "ffc910fd-e52f-4829-a649-5f2c44a9fce8");

        Course cse110WI22L = new Course(    //this should appear in the list
                courseId++,
                -1,
                2022,
                "WI",
                "CSE",
                "110",
                "Large");
        Course cse210FA21S = new Course(    //this should not appear in the list
                courseId++,
                -1,
                2021,
                "FA",
                "CSE",
                "210",
                "Small");

        List<Course> bobCourses = new ArrayList<>();
        bobCourses.add(cse110WI22L);
        bobCourses.add(cse210FA21S);

        StudentWithCourses BobAndCourses = new StudentWithCourses(Bob, bobCourses, "");

        scenario.onActivity( activity -> {
            //use test db and automatically mock Bill without going to NMM
            activity.setMockedStudent(BobAndCourses);
            activity.setDb(db);

            //get reference to list of students in homepage viewadapter:
            ArrayList<Student> viewAdapterList =
                    (ArrayList<Student>) activity.getStudentsViewAdapter().getStudents();

            //emulate clicking start search to start the FakedMessageListener;
            activity.onStartSearchingClicked();

            //Bob should now be visible in viewAdapterList
            Assert.assertEquals(Bob, viewAdapterList.get(0));
            Assert.assertEquals(1, viewAdapterList.size());

            //Bob should also have been inserted into db as second student
            Assert.assertEquals(Bob, db.studentsDao().get(2));

            //Bob should only have one course in the list
            List<Course> bobExpectedCourses = new ArrayList<>();
            bobExpectedCourses.add(cse110WI22L);

            Assert.assertEquals(bobExpectedCourses,
                    db.coursesDao().getForStudent(2));

            // emulate clicking stop, then start again to test whether Bob is
            // added twice
            Assert.assertEquals(2, db.studentsDao().getAll().size());
        });
    }

}