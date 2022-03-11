package edu.ucsd.cse110.bof;

import java.util.List;

import edu.ucsd.cse110.bof.model.IStudent;
import edu.ucsd.cse110.bof.model.StudentWithCourses;
import edu.ucsd.cse110.bof.model.db.Course;
import edu.ucsd.cse110.bof.model.db.Student;

public interface IBuilder {

    IBuilder setStuName(String name);
    IBuilder setStuPhotoURL(String photoURL);
    IBuilder setStuUUID(String UUID);
    IBuilder setStudent(Student student);
    IBuilder addCourse(int year, String quarter, String subj,
                              String number, String size);
    IBuilder setCourses(List<Course> courses);
    IBuilder setFromCSV(String csv);
    StudentWithCourses getSWC();
    void reset();

}
