package edu.ucsd.cse110.bof;

import android.app.Application;
import android.util.Log;

import java.util.ArrayList;
import java.util.Scanner;

import edu.ucsd.cse110.bof.model.IStudent;
import edu.ucsd.cse110.bof.model.db.AppDatabase;
import edu.ucsd.cse110.bof.model.db.Course;
import edu.ucsd.cse110.bof.model.db.Student;

//TODO test: after editing it to extend application
//TODO test: mocking student with wave and without
public class MockedStudentFactory extends Application {

    private static final String TAG = "McokedStudentFactoryLog";

    /**
     * Parses CSV and returns a StudentWithCourses
     * @param csv csv to parse as a String
     * @return StudentWithCourses
     */
    public StudentWithCourses makeMockedStudent(String csv) {
        Log.d(TAG, "Within factory, reading csv: " + csv);
        //TODO: valid csv check
        if (csv == null || csv.isEmpty()) {
            return null;
        }
        Scanner reader = new Scanner(csv);
        reader.useDelimiter("\\s*[,\n]\\s*");

        Student mockStudent = new Student();

        mockStudent.setUUID(reader.next());
        reader.nextLine();

        mockStudent.setName(reader.next());
        reader.nextLine();

        mockStudent.setPhotoUrl(reader.next());
        //reader.nextLine();

        ArrayList<Course> mockStuCourses = new ArrayList<>();

        int year;
        String quarter, subject, courseNum;
        String courseSize;
        String targetUUID;
        String wave;

        AppDatabase db = AppDatabase.singleton(getApplicationContext());
        IStudent thisStudent = db.studentsDao().get(1);

        while (reader.hasNextLine()) {
            reader.nextLine();
            if (!reader.hasNext()) { break; }

            try {
                year = Integer.parseInt(reader.next());
                quarter = reader.next();
                subject = reader.next();
                courseNum = reader.next();
                courseSize = reader.next();

                mockStuCourses.add(new Course(1, 1, year,
                        quarter, subject, courseNum, courseSize));
            } catch (NumberFormatException e) { //TODO test: if this way works
                targetUUID = reader.next();
                if(thisStudent.getUUID().equals(targetUUID) && reader.next().equals("wave")) //TODO test: possibly unexpected behavior from second part
                    mockStudent.wavedAtMe = true;
            }
        }

        reader.close();

        return new StudentWithCourses(mockStudent, mockStuCourses);
    }
}
