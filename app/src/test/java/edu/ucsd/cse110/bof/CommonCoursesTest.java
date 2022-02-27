package edu.ucsd.cse110.bof;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.*;

import android.content.Context;

import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import edu.ucsd.cse110.bof.model.db.AppDatabase;
import edu.ucsd.cse110.bof.model.db.Course;
import edu.ucsd.cse110.bof.model.db.CoursesDao;
import edu.ucsd.cse110.bof.model.db.Student;
import edu.ucsd.cse110.bof.model.db.StudentsDao;

@RunWith(AndroidJUnit4.class)
public class CommonCoursesTest {
    public static int courseId = 1;
    static final Course cse12SP21 = new Course(
            courseId++,
            1,
            2021,
            "SP",
            "CSE",
            "12"
            ,"Large");
    static final Course cse100FA22 = new Course(
            courseId++,
            1,
            2022,
            "FA",
            "CSE",
            "100"
            ,"Large");
    static final Course cse110WI22 = new Course(
            courseId++,
            1,
            2022,
            "WI",
            "CSE",
            "110"
            ,"Large");
    static final Course cse101WI22 = new Course(
            courseId++,
            1,
            2022,
            "WI",
            "CSE",
            "101"
            ,"Large");

    static final Course cse12SP21_2 = new Course(
            courseId++,
            2,
            2021,
            "SP",
            "CSE",
            "12"
            ,"Large");
    static final Course cse100FA22_2 = new Course(
            courseId++,
            2,
            2022,
            "FA",
            "CSE",
            "100"
            ,"Large");
    static final Course cse110WI22_2 = new Course(
            courseId++,
            2,
            2022,
            "WI",
            "CSE",
            "110"
            ,"Large");
    static final Course cse101WI22_2 = new Course(
            courseId++,
            2,
            2022,
            "WI",
            "CSE",
            "101"
            ,"Large");

    private List<Course> stu1Courses;
    private List<Course> stu2Courses;
    @Before
    public void init() {
        stu1Courses = new ArrayList<>();
        stu2Courses = new ArrayList<>();
    }

    @Test
    public void onlyOneCommonClasses() {
        stu1Courses.add(cse12SP21);
        stu1Courses.add(cse100FA22);
        stu2Courses.add(cse12SP21_2);
        stu2Courses.add(cse110WI22_2);

       List<Course> commonClassesFromMethod =
               BoFsTracker.getCommonCourses(stu1Courses,stu2Courses);

        //actual answer
        List<Course> commonClasses = new ArrayList<>();
        commonClasses.add(cse12SP21);
        Collections.sort(commonClasses, new BoFsTracker.SortbyYearAndQuarter());

        //check that answer from method matches actual answer
        assertEquals(1,commonClassesFromMethod.size());
        assertEquals(commonClasses, commonClassesFromMethod);
    }

    @Test
    public void noCommonClasses() {

        stu1Courses.add(cse12SP21);
        stu1Courses.add(cse100FA22);
        stu2Courses.add(cse110WI22_2);
        stu2Courses.add(cse101WI22_2);

        List<Course> commonClassesFromMethod =
                BoFsTracker.getCommonCourses(stu1Courses, stu2Courses);

        //actual answer
        List<Course> commonClasses = new ArrayList<>();

        //check that answer from method matches actual answer
        assertEquals(0,commonClassesFromMethod.size());
        assertEquals(commonClasses, commonClassesFromMethod);
    }

    @Test
    public void multipleCommonClasses() {

        stu1Courses.add(cse12SP21);
        stu1Courses.add(cse100FA22);
        stu2Courses.add(cse12SP21_2);
        stu2Courses.add(cse100FA22_2);
        stu2Courses.add(cse101WI22_2);

        List<Course> commonClassesFromMethod =
                BoFsTracker.getCommonCourses(stu1Courses,stu2Courses);

        //actual answer
        List<Course> commonClasses = new ArrayList<>();
        commonClasses.add(cse12SP21);
        commonClasses.add(cse100FA22);
        Collections.sort(commonClasses, new BoFsTracker.SortbyYearAndQuarter());

        //check that answer from method matches actual answer
        assertEquals(2,commonClassesFromMethod.size());
        assertEquals(commonClasses, commonClassesFromMethod);
    }

    @Test
    public void typoInCourseSubject() {
        Course correctCse12SP21 = new Course(
                courseId++,
                1,
                2021,
                "SP",
                "CSE",
                "12"
                ,"Large");
        Course typoCse12SP21 = new Course(
                courseId++,
                2,
                2021,
                "SP",
                "CS",
                "12"
                ,"Large");

        //define stu1Courses and stu2Courses lists
        stu1Courses.add(correctCse12SP21);
        stu2Courses.add(typoCse12SP21);

        List<Course> commonClassesFromMethod =
                BoFsTracker.getCommonCourses(stu1Courses,stu2Courses);

        //actual answer
        List<Course> commonClasses = new ArrayList<>();

        //check that answer from method matches actual answer
        assertEquals(0,commonClassesFromMethod.size());
        assertEquals(commonClasses, commonClassesFromMethod);
    }
}

