package edu.ucsd.cse110.bof.model;

import java.io.Serializable;
import java.util.List;

import edu.ucsd.cse110.bof.model.db.Course;
import edu.ucsd.cse110.bof.model.db.Student;

/**
 * Object that packages a student, their courses, and their target for waving in UUID form
 */
public class StudentWithCourses implements Serializable {
    private Student student;
    private List<Course> courses;
    private String waveTarget;

    public StudentWithCourses(Student student, List<Course> courses, String waveTarget) {
        this.student = student;
        this.courses = courses;
        this.waveTarget = (waveTarget == null) ? "" : waveTarget;
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

    public void setWaveTarget(String waveTarget) {
        this.waveTarget = waveTarget;
    }
}

