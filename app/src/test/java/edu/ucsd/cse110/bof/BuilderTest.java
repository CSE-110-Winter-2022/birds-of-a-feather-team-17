package edu.ucsd.cse110.bof;

import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import edu.ucsd.cse110.bof.model.StudentWithCourses;
import edu.ucsd.cse110.bof.model.db.Course;
import edu.ucsd.cse110.bof.model.db.Student;

//checks that csv parsing works as intended (factory handles parsing)
@RunWith(AndroidJUnit4.class)
public class BuilderTest {

    private static final String someUUID1 = "a4ca50b6-941b-11ec-b909-0242ac120002";
    private static final String someUUID2 = "232dc5a5-b428-4ff0-88af-8817afc8e098";
    private static final String billPhotoURL = "https://lh3.googleusercontent.com/pw/AM-JKLXQ2ix4dg-PzLrPOSMOOy6M3PSUrijov9jCLXs4IGSTwN73B4kr-F6Nti_4KsiUU8LzDSGPSWNKnFdKIPqCQ2dFTRbARsW76pevHPBzc51nceZDZrMPmDfAYyI4XNOnPrZarGlLLUZW9wal6j-z9uA6WQ=w854-h924-no?authuser=0";


    private static final String billCSV = "a4ca50b6-941b-11ec-b909-0242ac120002,,,,\n" +
            "Bill,,,,\n" +
            "https://lh3.googleusercontent.com/pw/AM-JKLXQ2ix4dg-PzLrPOSMOOy6M3PSUrijov9jCLXs4IGSTwN73B4kr-F6Nti_4KsiUU8LzDSGPSWNKnFdKIPqCQ2dFTRbARsW76pevHPBzc51nceZDZrMPmDfAYyI4XNOnPrZarGlLLUZW9wal6j-z9uA6WQ=w854-h924-no?authuser=0,,,,\n" +
            "2021,FA,CSE,210,Large\n" +
            "2022,WI,CSE,110,Tiny\n" +
            "2022,SP,CSE,110,Gigantic\n";

    private static final String bobCSV = "a4ca50b6-941b-11ec-b909-0242ac120002,,,,\n" +
            "Bob,,,,\n" +
            "https://upload.wikimedia.org/wikipedia/en/c/c5/Bob_the_builder.jpg,,,,\n";
    private static final String waveAtUUID2 = someUUID2 + ",wave,,,\n";

    private IBuilder builder;

    @Before
    public void createBuilder() {
        builder = new StudentWithCoursesBuilder();
    }

    @Test
    public void builderParsesBillCSV() {
        // expected values
        Student billExpected = new Student("Bill", billPhotoURL, someUUID1);
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
        Student bobExpected = new Student("Bob", "https://upload.wikimedia" +
                ".org/wikipedia/en/c/c5/Bob_the_builder.jpg", someUUID1);
        List<Course> coursesExpected = new ArrayList<>();

        // builder handles parsing, should return a StudentWithCourses
        StudentWithCourses bobWithCourses = builder.setFromCSV(bobCSV).getSWC();

        // correct student object (compares url and name only)
        Assert.assertEquals(bobExpected, bobWithCourses.getStudent());

        // correct courses list (compares year, quarter, subj, courseNum, and
        // size only)
        Assert.assertEquals(coursesExpected, bobWithCourses.getCourses());
    }

    //does not use csv to build
    @Test
    public void builderCreatesBillCorrectly() {
        Student bobExpected = new Student("Bob", "https://upload.wikimedia" +
                ".org/wikipedia/en/c/c5/Bob_the_builder.jpg", someUUID1);
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
                .setStuUUID(someUUID1)
                .addCourse(2021, "FA", "CSE", "210", "Large")
                .addCourse(2022, "WI", "CSE", "110", "Tiny")
                .addCourse(2022, "SP", "CSE", "110", "Gigantic")
                .getSWC();

        Assert.assertEquals(bobExpected, actual.getStudent());
        Assert.assertEquals(coursesExpected, actual.getCourses());
    }

    //fails since stuName is never set
    @Test
    public void builderFailsIfFieldNotSet() {
        Assert.assertThrows(Contract.ViolationException.class, () -> {
                StudentWithCourses actual = builder
                //.setStuName("Bob") would go here
                .setStuPhotoURL("https://upload.wikimedia.org/wikipedia/en/c/c5/Bob_the_builder.jpg")
                .setStuUUID(someUUID1)
                .addCourse(2021, "FA", "CSE", "210", "Large")
                .addCourse(2022, "WI", "CSE", "110", "Tiny")
                .addCourse(2022, "SP", "CSE", "110", "Gigantic")
                .getSWC();
        });
    }

    @Test
    public void builderParsesWaving() {
        // expected values
        Student billExpected = new Student("Bill", billPhotoURL, someUUID1);

        //wave to UUID2
        billExpected.setWaveTarget(someUUID2);

        List<Course> coursesExpected = new ArrayList<>();
        coursesExpected.add(new Course(1 ,1 ,2021,
                "FA", "CSE", "210", "Large"));
        coursesExpected.add(new Course(1 ,1 ,2022,
                "WI", "CSE", "110", "Tiny"));
        coursesExpected.add(new Course(1 ,1 ,2022,
                "SP", "CSE", "110", "Gigantic"));

        // builder handles parsing, should return a StudentWithCourses
        StudentWithCourses actual =
                builder.setFromCSV(billCSV + waveAtUUID2).getSWC();

        Assert.assertEquals(billExpected, actual.getStudent());
        Assert.assertEquals(coursesExpected, actual.getCourses());

        //confirm that the actual waveTarget is set
        Assert.assertEquals(someUUID2, actual.getWaveTarget());

    }
}
