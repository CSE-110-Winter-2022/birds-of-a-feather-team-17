package edu.ucsd.cse110.bof;

import android.util.Log;

import java.util.ArrayList;
import java.util.Scanner;

import edu.ucsd.cse110.bof.model.db.Course;
import edu.ucsd.cse110.bof.model.db.Student;

public class MockedStudentFactory {

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
        mockStudent.setName(reader.next());
        reader.nextLine();

        mockStudent.setPhotoUrl(reader.next());
        //reader.nextLine();

        ArrayList<Course> mockStuCourses = new ArrayList<>();

        int year;
        String quarter, subject, courseNum;
        String courseSize;

        while (reader.hasNextLine()) {
            reader.nextLine();
            if (!reader.hasNext()) { break; }
            year = Integer.parseInt(reader.next());
            quarter = reader.next();
            subject = reader.next();
            courseNum = reader.next();
            courseSize = reader.next();

            mockStuCourses.add(new Course(1, 1, year,
                    quarter, subject, courseNum, courseSize));
        }

        reader.close();

        return new StudentWithCourses(mockStudent, mockStuCourses);
    }
}
