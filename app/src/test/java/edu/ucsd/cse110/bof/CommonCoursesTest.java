package edu.ucsd.cse110.bof;

import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

import edu.ucsd.cse110.bof.model.db.Course;
import edu.ucsd.cse110.bof.model.db.StudentWithCourses;

public class CommonCoursesTest {
    static final Course cse12SP21 = new Course(
            0,
            0,
            2021,
            "Spring",
            "CSE",
            "12");
    static final Course cse100FA22 = new Course(
            0,
            0,
            2022,
            "Fall",
            "CSE",
            "100");
    static final Course cse110WI22 = new Course(
            0,
            0,
            2022,
            "Winter",
            "CSE",
            "110");
    static final Course cse101WI22 = new Course(
            0,
            0,
            2022,
            "Winter",
            "CSE",
            "101");


    @Test
    public void onlyOneCommonClasses() {
        //define stu1 with classes list
        StudentWithCourses stu1 = new StudentWithCourses();
        List<String> stu1Courses = new ArrayList<>();
        stu1Courses.add(cse12SP21.info);
        stu1Courses.add(cse100FA22.info);
        stu1.courses = stu1Courses;

        //define stu2 with classes list
        StudentWithCourses stu2 = new StudentWithCourses();
        List<String> stu2Courses = new ArrayList<>();
        stu2Courses.add(cse12SP21.info);
        stu2Courses.add(cse110WI22.info);
        stu2.courses = stu2Courses;

        //get answer from method
        List<String> commonClassesFromMethod =
                BoFsTracker.getCommonCourses(stu1, stu2);

        //actual answer
        List<String> commonClasses = new ArrayList<>();
        commonClasses.add(cse12SP21.info);

        //check that answer from method matches actual answer
        assertEquals(commonClassesFromMethod.size(), 1);
        assertEquals(commonClassesFromMethod, commonClasses);
    }

    @Test
    public void noCommonClasses() {

        //define stu1 with classes list
        StudentWithCourses stu1 = new StudentWithCourses();
        List<String> stu1Courses = new ArrayList<>();
        stu1Courses.add(cse12SP21.info);
        stu1Courses.add(cse100FA22.info);
        stu1.courses = stu1Courses;

        //define stu2 with classes list
        StudentWithCourses stu2 = new StudentWithCourses();
        List<String> stu2Courses = new ArrayList<>();
        stu1Courses.add(cse110WI22.info);
        stu2Courses.add(cse101WI22.info);
        stu2.courses = stu2Courses;

        //get answer from method
        List<String> commonClassesFromMethod =
                BoFsTracker.getCommonCourses(stu1, stu2);

        //actual answer
        List<String> commonClasses = new ArrayList<>();

        //check that answer from method matches actual answer
        assertEquals(commonClassesFromMethod.size(), 0);
        assertEquals(commonClassesFromMethod, commonClasses);
    }

    @Test
    public void typoInCourseSubject() {
        Course correctCse12SP21 = new Course(
                0,
                0,
                2021,
                "Spring",
                "CSE",
                "12");
        Course typoCse12SP21 = new Course(
                0,
                0,
                2021,
                "Spring",
                "CSe",
                "12");

        //define stu1 with classes list
        StudentWithCourses stu1 = new StudentWithCourses();
        List<String> stu1Courses = new ArrayList<>();
        stu1Courses.add(correctCse12SP21.info);
        stu1.courses = stu1Courses;

        //define stu2 with classes list
        StudentWithCourses stu2 =
                new StudentWithCourses();
        List<String> stu2Courses = new ArrayList<>();
        stu1Courses.add(typoCse12SP21.info);
        stu2.courses = stu2Courses;

        //get answer from method
        List<String> commonClassesFromMethod =
                BoFsTracker.getCommonCourses(stu1, stu2);

        //actual answer
        List<String> commonClasses = new ArrayList<>();

        //check that answer from method matches actual answer
        assertEquals(commonClassesFromMethod.size(), 0);
        assertEquals(commonClassesFromMethod, commonClasses);
    }
}

