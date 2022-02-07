package edu.ucsd.cse110.bof;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import edu.ucsd.cse110.bof.model.IStudent;
import edu.ucsd.cse110.bof.model.db.Course;

public class BoFsTracker {
    /**
     * Checks the list of courses between two different StudentWithCourses
     * objects and returns a new list containing only those courses that are
     * common between them
     *
     * @param thisStu the user's StudentWithCourses object
     * @param otherStu the StudentWithCourses object received over Bluetooth
     * @return ArrayList containing all the common Courses between the two
     * StudentWithCourses, in no particular order
     */
    public static List<Course> getCommonCourses(IStudent thisStu,
                                                IStudent otherStu) {
        ArrayList<Course> commonCoursesList = new ArrayList<>();

        //add all of the other student's courses to the HashSet
        HashSet<Course> otherStuCourses = new HashSet<>(otherStu.getCourses());

        //check all of this user's courses
        ArrayList<Course> thisStuCourses =
                (ArrayList<Course>) thisStu.getCourses();
        for (int index = 0; index < thisStuCourses.size(); index++) {
            Course currCourse = thisStuCourses.get(index);

            //current course in thisStuCourses is already in HashSet,
            //this course is common between both people
            if ( otherStuCourses.contains( currCourse ) ) {
                commonCoursesList.add( currCourse );
            }
        }

        return commonCoursesList;
    }
}
