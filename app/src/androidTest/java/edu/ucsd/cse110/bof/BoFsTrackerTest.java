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
public class BoFsTrackerTest {
    static final Course cse12SP21 = new Course(
            1,
            2021,
            "SP",
            "CSE",
            "12");
    static final Course cse100FA22 = new Course(
            1,
            2022,
            "FA",
            "CSE",
            "100");
    static final Course cse110WI22 = new Course(
            1,
            2022,
            "WI",
            "CSE",
            "110");
    static final Course cse101WI22 = new Course(
            1,
            2022,
            "WI",
            "CSE",
            "101");

    static final Course cse12SP21_2 = new Course(
            2,
            2021,
            "SP",
            "CSE",
            "12");
    static final Course cse100FA22_2 = new Course(
            2,
            2022,
            "FA",
            "CSE",
            "100");
    static final Course cse110WI22_2 = new Course(
            2,
            2022,
            "WI",
            "CSE",
            "110");
    static final Course cse101WI22_2 = new Course(
            2,
            2022,
            "WI",
            "CSE",
            "101");

    private StudentsDao studentsDao;
    private CoursesDao coursesDao;
    private AppDatabase empty_db;
    private Student stu1, stu2;
    private List<Course> stu1Courses;
    private List<Course> stu2Courses;
    private Context context;

    //instantiate database, students, course lists
    @Before
    public void init() {
        context = ApplicationProvider.getApplicationContext();
        AppDatabase.useTestSingleton(context);
        empty_db = AppDatabase.singleton(context);
        studentsDao = empty_db.studentsDao();
        coursesDao = empty_db.coursesDao();

        //define stu1 with classes list
        stu1 = new Student("Bob","photo1.jpg");
        studentsDao.insert(stu1);
        stu1Courses = new ArrayList<>();

        //define stu2 with classes list
        stu2 = new Student("Mary", "photo2.jpg");
        studentsDao.insert(stu2);
        stu2Courses = new ArrayList<>();

//        System.out.println("Stu1 name: "+studentsDao.get(1).getName());
//        System.out.println("Stu2 name: "+studentsDao.get(2).getName());
    }

    @After
    public void close() throws IOException {
        empty_db.close();
    }


    @Test
    public void onlyOneCommonClasses() {

        coursesDao.insert(cse12SP21);
        coursesDao.insert(cse100FA22);
        //stu1.courses = stu1Courses;

        //define stu2 with classes list
        coursesDao.insert(cse12SP21_2);
        coursesDao.insert(cse110WI22_2);
        //stu2.courses = stu2Courses;

        //get answer from method
        Student student1 = studentsDao.get(1);
        Student student2 = studentsDao.get(2);

        List<Course> commonClassesFromMethod =
                BoFsTracker.getCommonCourses(context, student1, student2);

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

        //define stu1 with classes list
        coursesDao.insert(cse12SP21);
        coursesDao.insert(cse100FA22);
        //stu1.courses = stu1Courses;

        //define stu2 with classes list
        coursesDao.insert(cse110WI22_2);
        coursesDao.insert(cse101WI22_2);
        //stu2.courses = stu2Courses;

        //get answer from method
        Student student1 = studentsDao.get(1);
        Student student2 = studentsDao.get(2);

        List<Course> commonClassesFromMethod =
                BoFsTracker.getCommonCourses(context, student1, student2);

        //actual answer
        List<Course> commonClasses = new ArrayList<>();

        //check that answer from method matches actual answer
        assertEquals(0,commonClassesFromMethod.size());
        assertEquals(commonClasses, commonClassesFromMethod);
    }

    @Test
    public void multipleCommonClasses() {

        //define stu1 with classes list
        coursesDao.insert(cse12SP21);
        coursesDao.insert(cse100FA22);
        //stu1.courses = stu1Courses;

        //define stu2 with classes list
        coursesDao.insert(cse12SP21_2);
        coursesDao.insert(cse100FA22_2);
        coursesDao.insert(cse101WI22_2);
        //stu2.courses = stu2Courses;

        //get answer from method
        Student student1 = studentsDao.get(1);
        Student student2 = studentsDao.get(2);

        List<Course> commonClassesFromMethod =
                BoFsTracker.getCommonCourses(context, student1, student2);

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
                1,
                2021,
                "SP",
                "CSE",
                "12");
        Course typoCse12SP21 = new Course(
                2,
                2021,
                "SP",
                "CSe",
                "12");

        //define stu1 with classes list
        stu1Courses.add(correctCse12SP21);
        //stu1.courses = stu1Courses;

        //define stu2 with classes list
        stu2Courses.add(typoCse12SP21);
        //stu2.courses = stu2Courses;

        //get answer from method
        Student student1 = studentsDao.get(1);
        Student student2 = studentsDao.get(2);

        List<Course> commonClassesFromMethod =
                BoFsTracker.getCommonCourses(context, student1, student2);

        //actual answer
        List<Course> commonClasses = new ArrayList<>();

        //check that answer from method matches actual answer
        assertEquals(0,commonClassesFromMethod.size());
        assertEquals(commonClasses, commonClassesFromMethod);
    }
}

