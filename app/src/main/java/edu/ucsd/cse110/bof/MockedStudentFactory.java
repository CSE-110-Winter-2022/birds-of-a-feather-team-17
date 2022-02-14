package edu.ucsd.cse110.bof;

import java.util.ArrayList;
import java.util.Scanner;

import edu.ucsd.cse110.bof.model.db.Course;
import edu.ucsd.cse110.bof.model.db.Student;

public class MockedStudentFactory {

    /**
     * Parses CSV and returns a StudentWithCourses
     * @param csv csv to parse as a String
     * @return StudentWithCourses
     */
    public StudentWithCourses makeMockedStudent(String csv) {
        if (csv == null) {
            return null;
        }
        Scanner reader = new Scanner(csv).useDelimiter("[, \n]");

        Student mockStudent = new Student();
        mockStudent.setName(reader.next());
        reader.nextLine();

        mockStudent.setPhotoUrl(reader.next());
        reader.nextLine();

        ArrayList<Course> mockStuCourses = new ArrayList<>();

        int year;
        String quarter, subject, courseNum;

        while (reader.hasNextLine()) {
            year = Integer.parseInt(reader.next());
            quarter = reader.next();
            subject = reader.next();
            courseNum = reader.next();
            reader.nextLine();

            mockStuCourses.add(new Course(1, 1, year,
                    quarter, subject, courseNum));
        }

        return new StudentWithCourses(mockStudent, mockStuCourses);
    }
}
