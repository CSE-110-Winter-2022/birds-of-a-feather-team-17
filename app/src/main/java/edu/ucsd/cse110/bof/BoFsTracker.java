package edu.ucsd.cse110.bof;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;

import edu.ucsd.cse110.bof.model.db.Course;

/**
 * Helper class for performing calculations on BoFs
 */
public class BoFsTracker {
    /**
     * Checks the list of courses between two different Student
     * objects and returns a new list containing only those courses that are
     * common between them
     *
     * @param thisStuCourses List of courses of this student (user)
     * @param otherStuCoursesList List of courses of other student
     * @return ArrayList containing all the common Courses between the two
     * Student, in order from oldest to newest
     */
    public static List<Course> getCommonCourses(List<Course> thisStuCourses, List<Course> otherStuCoursesList) {
        ArrayList<Course> commonCoursesList = new ArrayList<>();

        // Add all of the other student's courses to the HashSet
        HashSet<Course> otherStuCourses = new HashSet<>(otherStuCoursesList);

        // Check all of this user's courses
        for (int index = 0; index < thisStuCourses.size(); index++) {
            Course currCourse = thisStuCourses.get(index);

            //current course in thisStuCourses is already in HashSet,
            //this course is common between both people
            if ( otherStuCourses.contains( currCourse ) ) {
                commonCoursesList.add( currCourse );
            }
        }

        // Sort common courses
        Collections.sort(commonCoursesList, new SortbyYearAndQuarter());

        return commonCoursesList;
    }

    /**
     * Calculate the weight of one's courses based on class size
     * @param courses course list to calculate weight of
     * @return the weight in float
     */
    public static float calcClassSizeWeight(ArrayList<Course> courses) {
        if(courses == null) { return 0; }
        float sum = 0;
        for(Course c : courses) {
            switch(c.courseSize) {
                case "Tiny": sum += 1.00; break;
                case "Small": sum += 0.33; break;
                case "Medium": sum += 0.18; break;
                case "Large": sum += 0.10; break;
                case "Huge": sum += 0.06; break;
                case "Gigantic": sum += 0.01; break;
            }
        }
        return sum;
    }

    /**
     * Calculate the weight of one's courses based on recency
     * @param courses course list to calculate the weight of
     * @return the weight in float
     */
    public static int calcRecencyWeight(ArrayList<Course> courses) {
        if(courses == null) { return 0; }
        int sum = 0;
        for(Course c : courses) {
            if(c.year == 2022 && c.quarter.equals("WI")) {
                sum += 5;
                continue;
            }

            if(2022 - c.year > 1) {
                sum += 1;
            } else {
                switch (c.quarter) {
                    case "FA": sum += 5; break;
                    case "SP": sum += 3; break;
                    case "WI": sum += 2; break;
                    default: sum += 4;
                }
            }
        }
        return sum;
    }

    /**
     * Helper class to sort by year and quarter
     */
    static class SortbyYearAndQuarter implements Comparator<Course> {
        // Sort from oldest to newest courses
        public int compare(Course a, Course b) {
            int year1 = a.year;
            int year2 = b.year;
            String quarter1 = a.quarter;
            String quarter2 = b.quarter;

            if (year1 == year2) {
                return compareQuarter(quarter1, quarter2);
            }
            else {
                return year1-year2;
            }
        }
        private int compareQuarter(String q1, String q2) {

            // Used as placeholder: FA comes before R; SP/SS1/SS2/SSS come after R
            if (q2.equals("WI")) { q2 = "R"; }

            if (q1.equals("FA")) {
                // Quarter 1 always older/equal to quarter 2
                return -1;
            }
            else if (q1.equals("WI")) {
                String temp = "R";
                return temp.compareTo(q2);
            }
            else if (q1.equals("SP")) { return q1.compareTo(q2); }
            else if (q1.equals("SS1")) {return q1.compareTo(q2);}
            else if (q1.equals("SS2")) {return q1.compareTo(q2);}
            else {return q1.compareTo(q2);}
        }
    }
}
