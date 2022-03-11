package edu.ucsd.cse110.bof;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import androidx.lifecycle.Lifecycle;
import androidx.test.core.app.ActivityScenario;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.google.android.gms.nearby.messages.Message;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.List;

import edu.ucsd.cse110.bof.homepage.HomePageActivity;
import edu.ucsd.cse110.bof.model.StudentWithCourses;
import edu.ucsd.cse110.bof.model.db.AppDatabase;
import edu.ucsd.cse110.bof.model.db.Course;
import edu.ucsd.cse110.bof.model.db.Student;
import edu.ucsd.cse110.bof.viewProfile.StudentDetailActivity;

@RunWith(AndroidJUnit4.class)
public class WaveUnitTest {
    private AppDatabase db;
    private static int courseId = 1;
    private static int userId = 1;

    private StudentWithCourses BobAndCourses;
    private StudentWithCourses CaseyAndCourses;
    private Context context;

    private static final String avaUUID = "a4ca50b6-941b-11ec-b9090242ac120002";
    private static final String someUUID1 = "232dc5a5-b428-4ff0-88af-8817afc8e098";
    private static final String someUUID2 = "7299ef8f-3b21-45d3-b105-f9ceddca48bf";

    private static final String bobPhoto = "https://upload.wikimedia" +
            ".org/wikipedia/en/c/c5/Bob_the_builder.jpg";
    private static final String caseyPhoto = "https://commons.wikimedia" +
            ".org/wiki/File:Default_pfp.jpg";

    //create Ava's courses
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

    private static final Course cse100FA22S_Casey = new Course(
            courseId++,
            3,
            2022,
            "FA",
            "CSE",
            "100",
            "Small");
    private static final Course cse110WI22L_Casey = new Course(
            courseId++,
            3,
            2022,
            "WI",
            "CSE",
            "110",
            "Large");

    @Before
    public void createDatabaseAndUser() {
        //Create db
        context = ApplicationProvider.getApplicationContext();
        db = AppDatabase.useTestSingleton(context);
        //db = AppDatabase.singleton(context);

        //create Ava (user) and insert her into db, then get her dbID
        Student Ava = new Student();
        Ava.setUUID(avaUUID);
        db.studentsDao().insert(Ava);
        Ava.setStudentId(db.studentsDao().maxId());

        //add Ava's courses to db
        db.coursesDao().insert(cse100FA22S_Ava);
        db.coursesDao().insert(cse110WI22L_Ava);

        //insert Bob and his courses into db:
        Student Bob = new Student("Bob", bobPhoto, someUUID1);
        db.studentsDao().insert(Bob);

        List<Course> bobCourses = new ArrayList<>();
        bobCourses.add(cse110WI22L_Bob);
        bobCourses.add(cse210FA21S_Bob);

        //add Bob's courses to db
        db.coursesDao().insert(cse110WI22L_Bob);
        db.coursesDao().insert(cse210FA21S_Bob);

        BobAndCourses = new StudentWithCourses(Bob, bobCourses, "");

        //add Casey to db
        Student Casey = new Student("Casey", caseyPhoto, someUUID2);
        db.studentsDao().insert(Casey);

        List<Course> caseyCourses = new ArrayList<>();
        caseyCourses.add(cse110WI22L_Casey);
        caseyCourses.add(cse100FA22S_Casey);

        //add Casey's courses to db
        db.coursesDao().insert(cse100FA22S_Casey);
        db.coursesDao().insert(cse110WI22L_Casey);

        CaseyAndCourses = new StudentWithCourses(Casey, caseyCourses, "");
    }

    //tests that clicking wave icon updates the db (does not test if ui changes)
    @Test
    public void testUserWavesAtBob() {
        Intent intent = new Intent(context, StudentDetailActivity.class);
        intent.putExtra("student_id", 2);

        ActivityScenario<StudentDetailActivity> scenario =
                ActivityScenario.launch(intent);

        scenario.onActivity( activity -> {
            //use test db
            activity.onWaveClicked(null);
            Assert.assertTrue(db.studentsDao().get(2).isWavedTo());
        });
    }


    @Test
    public void testBobWavesAtUser() {
        BobAndCourses.setWaveTarget(avaUUID);

        ActivityScenario<HomePageActivity> scenario =
                ActivityScenario.launch(HomePageActivity.class);

        scenario.moveToState(Lifecycle.State.CREATED);

        scenario.onActivity( activity -> {
            //use test db

            activity.setMockedStudent(BobAndCourses);
            activity.setDb(db);

            //click start to add Bob (without wave)
            activity.onStartSearchingClicked();

            BobAndCourses.setWaveTarget(avaUUID);

            //call listener to update list with Bob waving
            activity.getRealListener().onFound(new Message(studentWithCoursesBytesFactory.convert(BobAndCourses)));

            Assert.assertTrue(db.studentsDao().get(2).isWavedAtMe());
            Assert.assertTrue(activity.getStudentsViewAdapter().getStudents().get(0).equals(BobAndCourses.getStudent()));
        });
    }

    @Test
    public void testBobWavingIncreasesPriority() {
        BobAndCourses.setWaveTarget(avaUUID);

        ActivityScenario<HomePageActivity> scenario =
                ActivityScenario.launch(HomePageActivity.class);

        scenario.moveToState(Lifecycle.State.CREATED);

        scenario.onActivity( activity -> {
            //use test db

            activity.setMockedStudent(BobAndCourses);
            activity.setDb(db);

            //click start to add Bob (without wave)
            activity.onStartSearchingClicked();

            BobAndCourses.setWaveTarget(avaUUID);

            //add Casey to the homepage list
            activity.getRealListener().onFound(new Message(studentWithCoursesBytesFactory.convert(CaseyAndCourses)));

            //confirm Casey is at top
            Assert.assertEquals( 2,
                    activity.getStudentsViewAdapter().getStudents().size());
            Assert.assertEquals( CaseyAndCourses.getStudent(),
                    activity.getStudentsViewAdapter().getStudents().get(0));

            //call listener to update list with Bob waving
            activity.getRealListener().onFound(new Message(studentWithCoursesBytesFactory.convert(BobAndCourses)));

            //confirm Bob is now waving in db and at top of homepage list
            Assert.assertTrue(db.studentsDao().get(2).isWavedAtMe());
            Assert.assertEquals( BobAndCourses.getStudent(),
                    activity.getStudentsViewAdapter().getStudents().get(0));        });
    }
}
