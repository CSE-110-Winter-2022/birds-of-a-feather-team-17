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
import edu.ucsd.cse110.bof.model.db.AppDatabase;
import edu.ucsd.cse110.bof.model.db.Course;
import edu.ucsd.cse110.bof.model.db.Student;
@RunWith(AndroidJUnit4.class)
public class SortBoFsTest {

    private AppDatabase db;
    private static int courseId = 1;
    private static int userId;

    private static final String bobPhoto = "https://upload.wikimedia" +
            ".org/wikipedia/en/c/c5/Bob_the_builder.jpg";

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
        userId = db.studentsDao().maxId();

        //add Ava's courses to db
        db.coursesDao().insert(cse12FA21S);
        db.coursesDao().insert(cse100FA21S);
        db.coursesDao().insert(cse110WI22L);
    }

    //FIXME: notifyItemInserted problem causes this to fail

    /*
    @Test
    public void sortByRecencySwitchesPositions() {
        //use ActivityScenario to emulate activities
        ActivityScenario<HomePageActivity> scenario =
                ActivityScenario.launch(HomePageActivity.class);

        //Create new student Bob with 2 common courses, both in FA21
        Student Bob = new Student("Bob", bobPhoto);

        ArrayList<Course> bobCourses = new ArrayList<>();
        bobCourses.add(cse12FA21S);
        bobCourses.add(cse100FA21S);

        StudentWithCourses BobAndCourses = new StudentWithCourses(Bob, bobCourses);

        //Create new student Casey with 1 common course in WI22
        Student Casey = new Student("Casey", "whatever.jpg");

        ArrayList<Course> caseyCourses = new ArrayList<>();
        caseyCourses.add(cse110WI22L);

        StudentWithCourses CaseyAndCourses = new StudentWithCourses(Casey,
                caseyCourses);


        //move to CREATED state to initalize variables in HomePageActivity
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

            //Bob should have two courses in the list/database
            Assert.assertEquals(2, Bob.getMatches());

            // create a second FakedMessageListener by stopping search and
            // restarting with new mockedStudent
            activity.onStopSearchingClicked();
            activity.setMockedStudent(CaseyAndCourses);
            activity.onStartSearchingClicked();

            // Bob should now be at the top (assuming default sort by matches)
            // followed by Casey
            Assert.assertEquals(2, viewAdapterList.size());
            Assert.assertEquals(Bob, viewAdapterList.get(0));
            Assert.assertEquals(Casey, viewAdapterList.get(1));

            //Casey should have 1 course
            Assert.assertEquals(1, Casey.getMatches());

            //Then sort list in studentsViewAdapter by recent
            activity.getStudentsViewAdapter().sortList("recent");

            //Casey is now above Bob
            Assert.assertEquals(2, viewAdapterList.size());
            Assert.assertEquals(Casey, viewAdapterList.get(0));
            Assert.assertEquals(Bob, viewAdapterList.get(1));
        });
    }


     */

}
