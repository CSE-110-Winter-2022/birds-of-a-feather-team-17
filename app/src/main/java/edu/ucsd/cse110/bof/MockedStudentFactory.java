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
        //TODO: valid csv check
        if (csv == null || csv.isEmpty()) {
            return null;
        }
        Scanner reader = new Scanner(csv);
        reader.useDelimiter("[, \n]");

        Student mockStudent = new Student();
        mockStudent.setName(reader.next());

        mockStudent.setPhotoUrl(reader.next());

        ArrayList<Course> mockStuCourses = new ArrayList<>();

        int year;
        String quarter, subject, courseNum;
        String courseSize;

        while (reader.hasNext()) {
            if (reader.next().isEmpty()) { break; }
            year = Integer.parseInt(reader.next());
            quarter = reader.next();
            subject = reader.next();
            courseNum = reader.next();
            courseSize = reader.next();
            //reader.nextLine();

            mockStuCourses.add(new Course(1, 1, year,
                    quarter, subject, courseNum, courseSize));
        }

        reader.close();

        return new StudentWithCourses(mockStudent, mockStuCourses);
    }
}
