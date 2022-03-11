package edu.ucsd.cse110.bof;

import android.app.Application;
import android.util.Log;

import java.util.ArrayList;
import java.util.Scanner;

import edu.ucsd.cse110.bof.model.db.Course;
import edu.ucsd.cse110.bof.model.db.Student;

//TODO test: after editing it to extend application
//TODO test: mocking student with wave and without
public class MockedStudentFactory {

    private static final String TAG = "McokedStudentFactoryLog";

    /**
     * Parses CSV and returns a StudentWithCourses
     * @param csv csv to parse as a String
     * @return StudentWithCourses
     */
    public StudentWithCourses makeMockedStudent(String csv) {
        Log.d(TAG, "Within factory, reading csv: " + csv);
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

        while (reader.hasNextLine()) {
            reader.nextLine();
            if (!reader.hasNext()) { break; }

           if(reader.hasNextInt()) {
                year = Integer.parseInt(reader.next());
                quarter = reader.next();
                subject = reader.next();
                courseNum = reader.next();
                courseSize = reader.next();

                mockStuCourses.add(new Course(1, 1, year,
                        quarter, subject, courseNum, courseSize));
            }
           else {
                targetUUID = reader.next();
                if(reader.next().equals("wave")) //TODO test: possibly unexpected behavior from second part
                    mockStudent.setWaveTarget(targetUUID);
            }
        }

        reader.close();

        return new StudentWithCourses(mockStudent, mockStuCourses);
    }
}