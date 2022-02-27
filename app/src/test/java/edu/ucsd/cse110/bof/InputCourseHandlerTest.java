package edu.ucsd.cse110.bof;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import android.content.Context;
import android.util.Log;

import androidx.lifecycle.Lifecycle;
import androidx.room.Room;
import androidx.test.core.app.ActivityScenario;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.IOException;

import edu.ucsd.cse110.bof.InputCourses.InputCourseActivity;
import edu.ucsd.cse110.bof.InputCourses.InputCourseHandler;
import edu.ucsd.cse110.bof.model.db.AppDatabase;
import edu.ucsd.cse110.bof.model.db.Course;

@RunWith(AndroidJUnit4.class)
public class InputCourseHandlerTest {
    private Context context;
    private AppDatabase db;

    @Before
    public void createDB() {
        context = ApplicationProvider.getApplicationContext();
        AppDatabase.useTestSingleton(context);
        db = AppDatabase.singleton(context);

    }

    @After
    public void closeDB() throws IOException {
        db.close();
    }

    @Test
    public void courseIsInvalid() {
            InputCourseHandler inputCourseHandler = new InputCourseHandler(context);
            Course newCourse = inputCourseHandler.inputCourse(0, 2021, "SP", "CSE", "");
            assertNull(newCourse);
            newCourse = inputCourseHandler.inputCourse(0, 2021, "SP", "", "12");
            assertNull(newCourse);
            newCourse = inputCourseHandler.inputCourse(0, 2021, "", "CSE", "12");
            assertNull(newCourse);
            newCourse = inputCourseHandler.inputCourse(0, 2021, "", "", "");
            assertNull(newCourse);
    }

    @Test
    public void databaseIsEmpty() {
        InputCourseHandler inputCourseHandler = new InputCourseHandler(context);
        Course newCourse = inputCourseHandler.inputCourse(0, 2021, "SP", "CSE", "12");
        assertNotNull(newCourse);
        assertFalse(inputCourseHandler.getIsDuplicate());
        assertEquals(newCourse.quarter, "SP");
        assertEquals(newCourse.year, 2021);
        assertEquals(newCourse.subject, "CSE");
        assertEquals(newCourse.courseNum, "12");
    }

    @Test
    public void databaseHasNoDuplicate() {
        db.coursesDao().insert(new Course(
                db.coursesDao().maxId() + 1,
                1,
                2021,
                "SP",
                "CSE",
                "12"));
        InputCourseHandler inputCourseHandler = new InputCourseHandler(context);
        Course newCourse = inputCourseHandler.inputCourse(
                db.coursesDao().maxId() + 1,
                2022,
                "FA",
                "CSE",
                "100");
        assertNotNull(newCourse);
        assertFalse(inputCourseHandler.getIsDuplicate());
    }

    @Test
    public void databaseHasDuplicates() {
        System.out.println(Integer.toString(db.coursesDao().count()));
        db.coursesDao().insert(new Course(
                db.coursesDao().maxId() + 1,
                1,
                2021,
                "SP",
                "CSE",
                "12"));
        db.coursesDao().insert(new Course(
                db.coursesDao().maxId() + 1,
                1,
                2022,
                "SS1",
                "HILD",
                "7B"));
        db.coursesDao().insert(new Course(
                db.coursesDao().maxId() + 1,
                1,
                2022,
                "WI",
                "CSE",
                "101"));
        InputCourseHandler inputCourseHandler = new InputCourseHandler(context);
        Course newCourse = inputCourseHandler.inputCourse(db.coursesDao().maxId() + 1, 2021, "SP", "CSE", "12");
        assertNull(newCourse);
        assertTrue(inputCourseHandler.getIsDuplicate());

        inputCourseHandler = new InputCourseHandler(context);
        newCourse = inputCourseHandler.inputCourse(db.coursesDao().maxId() + 1, 2022, "SS1", "Hild", "7B");
        assertNull(newCourse);
        assertTrue(inputCourseHandler.getIsDuplicate());

        inputCourseHandler = new InputCourseHandler(context);
        newCourse = inputCourseHandler.inputCourse(db.coursesDao().maxId() + 1, 2017, "SSS", "DOC", "2");
        assertNotNull(newCourse);
        assertFalse(inputCourseHandler.getIsDuplicate());
    }
}