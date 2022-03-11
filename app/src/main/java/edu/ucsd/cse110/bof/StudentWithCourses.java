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
    private String waveTarget;

    public StudentWithCourses(Student student, List<Course> courses, String waveTarget) {
        this.student = student;
        this.courses = courses;
        this.waveTarget = waveTarget;
    }

    public Student getStudent() {
        return student;
    }

    public List<Course> getCourses() {
        return courses;
    }

    public String getWaveTarget() { return waveTarget; }

    public void setCourses(List<Course> courses) {
        this.courses = courses;
    }
}

