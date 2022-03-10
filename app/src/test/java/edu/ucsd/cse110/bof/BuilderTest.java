package edu.ucsd.cse110.bof;

import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.ArrayList;
import java.util.List;

import edu.ucsd.cse110.bof.model.db.Course;
import edu.ucsd.cse110.bof.model.db.Student;

//checks that csv parsing works as intended (factory handles parsing)
@RunWith(AndroidJUnit4.class)
public class BuilderTest {

    private static final String billCSV = "Bill,,,,\n" +
            "https://lh3.googleusercontent.com/pw/AM-JKLXQ2ix4dg-PzLrPOSMOOy6M3PSUrijov9jCLXs4IGSTwN73B4kr-F6Nti_4KsiUU8LzDSGPSWNKnFdKIPqCQ2dFTRbARsW76pevHPBzc51nceZDZrMPmDfAYyI4XNOnPrZarGlLLUZW9wal6j-z9uA6WQ=w854-h924-no?authuser=0,,,,\n" +
            "2021,FA,CSE,210,Large\n" +
            "2022,WI,CSE,110,Tiny\n" +
            "2022,SP,CSE,110,Gigantic\n";

    private static final String bobCSV = "Bob,,,,\n" +
            "https://upload.wikimedia.org/wikipedia/en/c/c5/Bob_the_builder.jpg,,,,\n";
    private IBuilder builder;

    @Before
    public void createBuilder() {
        builder = new StudentWithCoursesBuilder();
    }

    @Test
    public void builderParsesBillCSV() {
        // expected values
        Student billExpected = new Student("Bill", "https://lh3.googleusercontent.com/pw/AM-JKLXQ2ix4dg-PzLrPOSMOOy6M3PSUrijov9jCLXs4IGSTwN73B4kr-F6Nti_4KsiUU8LzDSGPSWNKnFdKIPqCQ2dFTRbARsW76pevHPBzc51nceZDZrMPmDfAYyI4XNOnPrZarGlLLUZW9wal6j-z9uA6WQ=w854-h924-no?authuser=0");
        List<Course> coursesExpected = new ArrayList<>();
        coursesExpected.add(new Course(1 ,1 ,2021,
                "FA", "CSE", "210", "Large"));
        coursesExpected.add(new Course(1 ,1 ,2022,
                "WI", "CSE", "110", "Tiny"));
        coursesExpected.add(new Course(1 ,1 ,2022,
                "SP", "CSE", "110", "Gigantic"));

        // builder handles parsing, should return a StudentWithCourses
        StudentWithCourses billWithCourses = builder.setFromCSV(billCSV).getSWC();

        // correct student object (compares url and name only)
        Assert.assertEquals(billExpected, billWithCourses.getStudent());

        // correct courses list (compares year, quarter, subj, courseNum, and
        // size only)
        Assert.assertEquals(coursesExpected, billWithCourses.getCourses());
    }

    //identical to above test, but Bob has no courses
    @Test
    public void builderParsesBobCSV() {
        // expected values
        Student bobExpected = new Student("Bob", "https://upload.wikimedia.org/wikipedia/en/c/c5/Bob_the_builder.jpg");
        List<Course> coursesExpected = new ArrayList<>();

        // builder handles parsing, should return a StudentWithCourses
        StudentWithCourses bobWithCourses = builder.setFromCSV(bobCSV).getSWC();

        // correct student object (compares url and name only)
        Assert.assertEquals(bobExpected, bobWithCourses.getStudent());

        // correct courses list (compares year, quarter, subj, courseNum, and
        // size only)
        Assert.assertEquals(coursesExpected, bobWithCourses.getCourses());
    }

    @Test
    public void builderCreatesBillCorrectly() {
        Student bobExpected = new Student("Bob", "https://upload.wikimedia.org/wikipedia/en/c/c5/Bob_the_builder.jpg");
        List<Course> coursesExpected = new ArrayList<>();

        coursesExpected.add(new Course(1 ,1 ,2021,
                "FA", "CSE", "210", "Large"));
        coursesExpected.add(new Course(1 ,1 ,2022,
                "WI", "CSE", "110", "Tiny"));
        coursesExpected.add(new Course(1 ,1 ,2022,
                "SP", "CSE", "110", "Gigantic"));

        StudentWithCourses actual = builder
                .setStuName("Bob")
                .setStuPhotoURL("https://upload.wikimedia.org/wikipedia/en/c/c5/Bob_the_builder.jpg")
                .addCourse(2021, "FA", "CSE", "210", "Large")
                .addCourse(2022, "WI", "CSE", "110", "Tiny")
                .addCourse(2022, "SP", "CSE", "110", "Gigantic")
                .getSWC();

        Assert.assertEquals(bobExpected, actual.getStudent());
        Assert.assertEquals(coursesExpected, actual.getCourses());
    }
}
