package edu.ucsd.cse110.bof;

import android.os.Parcel;
import android.os.Parcelable;

import java.io.Serializable;
import java.util.List;
import java.util.Observable;
import java.util.Observer;

import edu.ucsd.cse110.bof.model.IStudent;
import edu.ucsd.cse110.bof.model.db.Course;
import edu.ucsd.cse110.bof.model.db.Student;

public class StudentWithCourses implements Serializable {
    private Student student;
    private List<Course> courses;


    public StudentWithCourses() {
        this.student = null;
        this.courses = null;
    }

    public StudentWithCourses(Student student, List<Course> courses) {
        this.student = student;
        this.courses = courses;
    }

    public synchronized Student getStudent() {
        return student;
    }

    public synchronized List<Course> getCourses() {
        return courses;
    }

    public void copy(StudentWithCourses studentWithCourses) {
        this.student = studentWithCourses.getStudent();
        this.courses = studentWithCourses.getCourses();
    }

    public void setStudent(Student student) {
        this.student = student;
    }

    public void setCourses(List<Course> courses) {
        this.courses = courses;
    }
}

