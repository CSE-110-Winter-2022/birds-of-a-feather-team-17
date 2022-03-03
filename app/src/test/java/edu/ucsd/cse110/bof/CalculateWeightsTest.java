package edu.ucsd.cse110.bof;

import static org.junit.Assert.*;

import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.checkerframework.checker.units.qual.A;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.ArrayList;

import edu.ucsd.cse110.bof.homepage.HomePageActivity;
import edu.ucsd.cse110.bof.model.db.Course;

@RunWith(AndroidJUnit4.class)
public class CalculateWeightsTest {

    @Test
    public void sampleClassSizeTest() {
        ArrayList<Course> commonCourses = new ArrayList<>();
        commonCourses.add(new Course(0, 0, 2021, "FA", "CSE", "110", "Tiny"));
        commonCourses.add(new Course(0, 0, 2021, "FA", "CSE", "110", "Large"));
        commonCourses.add(new Course(0, 0, 2021, "FA", "CSE", "110", "Huge"));
        float weight = HomePageActivity.calcClassSizeWeight(commonCourses);
        assertEquals(1.16, weight, 0.001);
    }

    @Test
    public void sampleRecencyTest() {
        ArrayList<Course> commonCourses = new ArrayList<>();
        commonCourses.add(new Course(0, 0, 2021, "FA", "CSE", "210", "Gigantic"));
        commonCourses.add(new Course(0, 0, 2021, "WI", "CSE", "210", "Gigantic"));
        int weight = HomePageActivity.calcRecencyWeight(commonCourses);
        assertEquals(7, weight);
    }

    @Test
    public void summerRecencyTest() {
        ArrayList<Course> commonCourses = new ArrayList<>();
        int weight = HomePageActivity.calcRecencyWeight(commonCourses);
        assertEquals(0, weight);
        commonCourses.add(new Course(0, 0, 2021, "SSS", "CSE", "210", "Gigantic"));
        weight = HomePageActivity.calcRecencyWeight(commonCourses);
        assertEquals(4, weight);
        commonCourses.add(new Course(0, 0, 2021, "SS1", "CSE", "210", "Gigantic"));
        weight = HomePageActivity.calcRecencyWeight(commonCourses);
        assertEquals(8, weight);
    }
}
