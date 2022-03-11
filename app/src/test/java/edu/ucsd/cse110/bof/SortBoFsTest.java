package edu.ucsd.cse110.bof;

import android.content.Context;
import android.util.Log;

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
import edu.ucsd.cse110.bof.homepage.StudentsViewAdapter;
import edu.ucsd.cse110.bof.model.db.AppDatabase;
import edu.ucsd.cse110.bof.model.db.Course;
import edu.ucsd.cse110.bof.model.db.Student;
@RunWith(AndroidJUnit4.class)
public class SortBoFsTest {

    private AppDatabase db;
    private static int courseId = 1;
    private static int userId;

    ActivityScenario<HomePageActivity> scenario;

    private static final String bobPhoto = "https://upload.wikimedia" +
            ".org/wikipedia/en/c/c5/Bob_the_builder.jpg";

    //create Ava's courses
    private static final Course c1 = new Course(
            courseId++,
            userId,
            2018,
            "SS1",
            "CSE",
            "210",
            "Tiny");
    private static final Course c2 = new Course(
            courseId++,
            userId,
            2019,
            "FA",
            "CSE",
            "110",
            "Tiny");
    private static final Course c3 = new Course(
            courseId++,
            userId,
            2018,
            "WI",
            "CSE",
            "110",
            "Large");
    private static final Course c4 = new Course(
            courseId++,
            userId,
            2021,
            "SSS",
            "CSE",
            "130",
            "Gigantic");
    private static final Course c5 = new Course(
            courseId++,
            userId,
            2021,
            "FA",
            "CSE",
            "130",
            "Huge");

    private Student Ava;
    private Student Bob;
    private Student Casey;
    private ArrayList<Course> bobCourses;
    private ArrayList<Course> caseyCourses;

    @Before
    public void init() {
        //Create db
        Context context = ApplicationProvider.getApplicationContext();
        AppDatabase.useTestSingleton(context);
        db = AppDatabase.singleton(context);

        //create Ava (user) and insert her into db, then get her dbID
        Ava = new Student();
        db.studentsDao().insert(Ava);
        Ava.setStudentId(db.studentsDao().maxId());
        userId = db.studentsDao().maxId();

        //add Ava's courses to db
        db.coursesDao().insert(c1);
        db.coursesDao().insert(c2);
        db.coursesDao().insert(c3);
        db.coursesDao().insert(c4);
        db.coursesDao().insert(c5);

        //launch HomePageActivity
        scenario = ActivityScenario.launch(HomePageActivity.class);
        scenario.moveToState(Lifecycle.State.CREATED);

        //create students
        Bob = new Student("Bob", bobPhoto, "ffc910fd-e52f-4829-a649-5f2c44a9fce4");

        bobCourses = new ArrayList<>();
        bobCourses.add(c1);
        bobCourses.add(c2);
        bobCourses.add(c3);

        Casey = new Student("Casey","", "efc910fd-e52f-4829-a649-5f2c44a9fce4" );
        caseyCourses = new ArrayList<>();
        caseyCourses.add(c4);
        caseyCourses.add(c5);

        db.studentsDao().insert(Bob);
        Bob.setStudentId(db.studentsDao().maxId());

        db.studentsDao().insert(Casey);
        Casey.setStudentId(db.studentsDao().maxId());
    }

    @Test
    public void sortByRecencySwitchesPositions() {
        scenario.onActivity( activity -> {
            //set weights for students
            Bob.setMatches(bobCourses.size());
            Bob.setClassSizeWeight(HomePageActivity.calcClassSizeWeight(bobCourses));
            Bob.setRecencyWeight(HomePageActivity.calcRecencyWeight(bobCourses));

            Casey.setMatches(caseyCourses.size());
            Casey.setClassSizeWeight(HomePageActivity.calcClassSizeWeight(caseyCourses));
            Casey.setRecencyWeight(HomePageActivity.calcRecencyWeight(caseyCourses));

            StudentsViewAdapter adp = activity.getStudentsViewAdapter();
            adp.setContext(activity.getApplicationContext());
            adp.addStudent(Bob);
            adp.setContext(activity.getApplicationContext());
            adp.addStudent(Casey);

            List<Student> studentsList = adp.getStudents();

            //Bob should now be visible in viewAdapterList
            Assert.assertEquals(Bob, studentsList.get(0));
            Assert.assertEquals(Casey, studentsList.get(1));
            Assert.assertEquals(2, studentsList.size());

            //sort by recent
            adp.sortList("recent");

            studentsList = adp.getStudents();

            //Casey should be at top
            Assert.assertEquals(2, studentsList.size());
            Assert.assertEquals(Casey, studentsList.get(0));
            Assert.assertEquals(Bob, studentsList.get(1));
        });
    }

    @Test
    public void sortBySizeSwitchesPositions() {
        scenario.onActivity( activity -> {
            //set weights for students
            Bob.setMatches(bobCourses.size());
            Bob.setClassSizeWeight(HomePageActivity.calcClassSizeWeight(bobCourses));
            Bob.setRecencyWeight(HomePageActivity.calcRecencyWeight(bobCourses));

            Casey.setMatches(caseyCourses.size());
            Casey.setClassSizeWeight(HomePageActivity.calcClassSizeWeight(caseyCourses));
            Casey.setRecencyWeight(HomePageActivity.calcRecencyWeight(caseyCourses));

            StudentsViewAdapter adp = activity.getStudentsViewAdapter();
            adp.setContext(activity.getApplicationContext());
            adp.addStudent(Bob);
            adp.setContext(activity.getApplicationContext());
            adp.addStudent(Casey);

            List<Student> studentsList = adp.getStudents();

            //Bob should now be visible in viewAdapterList
            Assert.assertEquals(Bob, studentsList.get(0));
            Assert.assertEquals(Casey, studentsList.get(1));
            Assert.assertEquals(2, studentsList.size());

            //sort by recent
            adp.sortList("recent");

            studentsList = adp.getStudents();

            //Casey should be at top
            Assert.assertEquals(2, studentsList.size());
            Assert.assertEquals(Casey, studentsList.get(0));
            Assert.assertEquals(Bob, studentsList.get(1));

            //sort by size
            adp.sortList("small classes");

            //Casey should be at top
            Assert.assertEquals(2, studentsList.size());
            Assert.assertEquals(Bob, studentsList.get(0));
            Assert.assertEquals(Casey, studentsList.get(1));
        });
    }

}
