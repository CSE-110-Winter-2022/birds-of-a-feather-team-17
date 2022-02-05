package edu.ucsd.cse110.bof;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import edu.ucsd.cse110.bof.model.IStudent;
import edu.ucsd.cse110.bof.model.db.Course;

public class BoFsTracker {
    /**
     * Checks the list of courses between two different IStudent
     * objects and returns a new list containing only those courses that are
     * common between them as Strings
     *
     * @param thisStu the user's IStudent object
     * @param otherStu the IStudent object received over Bluetooth
     * @return ArrayList containing all the common courses between the two
     * IStudents, as Strings in no particular order
     */
    public static List<String> getCommonCourses(IStudent thisStu,
                                                IStudent otherStu) {
        ArrayList<String> commonCoursesList = new ArrayList<>();

        //add all of the other student's courses to the HashSet
        HashSet<String> otherStuCourses = new HashSet<>(otherStu.getCourses());

        //check all of this user's courses
        ArrayList<String> thisStuCourses =
                (ArrayList<String>) thisStu.getCourses();
        for (int index = 0; index < thisStuCourses.size(); index++) {
            String currCourse = thisStuCourses.get(index);

            //current course in thisStuCourses is already in HashSet,
            //this course is common between both people
            if ( otherStuCourses.contains( currCourse ) ) {
                commonCoursesList.add( currCourse );
            }
        }

        return commonCoursesList;
    }
}
