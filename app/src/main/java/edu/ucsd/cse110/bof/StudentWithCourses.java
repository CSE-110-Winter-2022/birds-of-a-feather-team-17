package edu.ucsd.cse110.bof;

import android.os.Parcel;
import android.os.Parcelable;

import java.io.Serializable;
import java.util.List;

import edu.ucsd.cse110.bof.model.IStudent;
import edu.ucsd.cse110.bof.model.db.Course;
import edu.ucsd.cse110.bof.model.db.Student;

public class StudentWithCourses implements Serializable {
    private Student student;
    private List<Course> courses;

    public StudentWithCourses(Student student, List<Course> courses) {
        this.student = student;
        this.courses = courses;
    }

    public Student getStudent() {
        return student;
    }

    public List<Course> getCourses() {
        return courses;
    }

    public void setCourses(List<Course> courses) {
        this.courses = courses;
    }
}

