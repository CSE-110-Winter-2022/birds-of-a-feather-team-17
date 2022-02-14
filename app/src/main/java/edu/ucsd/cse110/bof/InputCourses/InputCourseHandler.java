package edu.ucsd.cse110.bof.InputCourses;

import android.content.Context;
import android.widget.Toast;

import java.util.HashSet;
import java.util.List;

import edu.ucsd.cse110.bof.model.db.AppDatabase;
import edu.ucsd.cse110.bof.model.db.Course;

public class InputCourseHandler {
    private static final int USER_ID = 1;   //id of user in db
    private int numEntered;                 //number of courses entered by this
    private AppDatabase db;                 //database instance
    private boolean isDuplicate = false;    //true if last entered course is
                                            // a duplicate

    /**
     * Constructor for InputCourseHandler
     * @param context context that this is called in
     * @param isTest true if this InputCourseHandler is for testing
     */
    public InputCourseHandler(Context context, boolean isTest) {
        if (isTest) {
            db = AppDatabase.useTestSingleton(context);
        }
        else {
            db = AppDatabase.singleton(context);
        }
        numEntered = 0;
        isDuplicate = false;
    }

    /**
     * numEntered getter
     * @return numEntered
     */
    public int getNumEntered() {
        return numEntered;
    }

    /**
     * Makes the new course based on the parameters, and adds it only if it
     * isn't a duplicate. Also sets isDuplicate flag for checking outside
     * this class
     * @param year          course's year
     * @param quarter       course's quarter
     * @param subject       course's subject
     * @param courseNum     course's number
     * @return Course if successfully made (regardless of whether inserted,
     * use isDuplicate(returnedCourse) to check if inserted) or null if no
     * Course made
     */
    public Course inputCourse(int courseId, int year, String quarter,
                               String subject, String courseNum) {
        if (quarter.equals("") || subject.equals("") || courseNum.equals("")) {
            return null;
        }

        Course newCourse = new Course(courseId, USER_ID, year, quarter,
                subject, courseNum);

        if (!isDuplicateCourse(newCourse)) {
            isDuplicate = false;
            db.coursesDao().insert(newCourse);
            numEntered++;
            return newCourse;
        }

        isDuplicate = true;
        return null;
    }

    /**
     * isDuplicate getter
     * @return isDuplicate
     */
    public boolean getIsDuplicate() {
        return isDuplicate;
    }

    /**
     * Checks if this course is already entered into this database
     * @param course
     * @return
     */
    private boolean isDuplicateCourse(Course course) {
        List<Course> userCourses = db.coursesDao().getForStudent(USER_ID);
        HashSet<Course> stuCourses = new HashSet<>(userCourses);
        return stuCourses.contains(course);
    }
}