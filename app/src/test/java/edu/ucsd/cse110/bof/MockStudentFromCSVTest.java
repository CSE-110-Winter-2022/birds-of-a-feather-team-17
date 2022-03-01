package edu.ucsd.cse110.bof;

import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import edu.ucsd.cse110.bof.model.db.Course;
import edu.ucsd.cse110.bof.model.db.Student;

//checks that csv parsing works as intended (factory handles parsing)
public class MockStudentFromCSVTest {

    private final MockedStudentFactory factory = new MockedStudentFactory();

    private static final String billCSV = "Bill,,,,\n" +
            "https://lh3.googleusercontent.com/pw/AM-JKLXQ2ix4dg-PzLrPOSMOOy6M3PSUrijov9jCLXs4IGSTwN73B4kr-F6Nti_4KsiUU8LzDSGPSWNKnFdKIPqCQ2dFTRbARsW76pevHPBzc51nceZDZrMPmDfAYyI4XNOnPrZarGlLLUZW9wal6j-z9uA6WQ=w854-h924-no?authuser=0,,,,\n" +
            "2021,FA,CSE,210,Large\n" +
            "2022,WI,CSE,110,Tiny\n" +
            "2022,SP,CSE,110,Gigantic\n";

    private static final String bobCSV = "Bob,,,,\n" +
            "https://upload.wikimedia.org/wikipedia/en/c/c5/Bob_the_builder.jpg,,,,\n";

    @Test
    public void factoryParsesBillCSV() {
        // expected values
        Student billExpected = new Student("Bill", "https://lh3.googleusercontent.com/pw/AM-JKLXQ2ix4dg-PzLrPOSMOOy6M3PSUrijov9jCLXs4IGSTwN73B4kr-F6Nti_4KsiUU8LzDSGPSWNKnFdKIPqCQ2dFTRbARsW76pevHPBzc51nceZDZrMPmDfAYyI4XNOnPrZarGlLLUZW9wal6j-z9uA6WQ=w854-h924-no?authuser=0");
        List<Course> coursesExpected = new ArrayList<>();
        coursesExpected.add(new Course(1 ,1 ,2021,
                "FA", "CSE", "210", "Large"));
        coursesExpected.add(new Course(1 ,1 ,2022,
                "WI", "CSE", "110", "Tiny"));
        coursesExpected.add(new Course(1 ,1 ,2022,
                "SP", "CSE", "110", "Gigantic"));

        // factory handles parsing, should return a StudentWithCourses
        StudentWithCourses billWithCourses = factory.makeMockedStudent(billCSV);

        // correct student object (compares url and name only)
        Assert.assertEquals(billExpected, billWithCourses.getStudent());

        // correct courses list (compares year, quarter, subj, courseNum, and
        // size only)
        Assert.assertEquals(coursesExpected, billWithCourses.getCourses());
    }

    //identical to above test, but Bob has no courses
    @Test
    public void factoryParsesBobCSV() {
        // expected values
        Student bobExpected = new Student("Bob", "https://upload.wikimedia.org/wikipedia/en/c/c5/Bob_the_builder.jpg");
        List<Course> coursesExpected = new ArrayList<>();

        // factory handles parsing, should return a StudentWithCourses
        StudentWithCourses bobWithCourses = factory.makeMockedStudent(billCSV);

        // correct student object (compares url and name only)
        Assert.assertEquals(bobExpected, bobWithCourses.getStudent());

        // correct courses list (compares year, quarter, subj, courseNum, and
        // size only)
        Assert.assertEquals(coursesExpected, bobWithCourses.getCourses());
    }
}
