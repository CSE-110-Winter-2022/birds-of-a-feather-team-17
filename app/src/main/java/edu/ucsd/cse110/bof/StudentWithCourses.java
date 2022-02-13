package edu.ucsd.cse110.bof;

import java.util.List;

import edu.ucsd.cse110.bof.model.IStudent;
import edu.ucsd.cse110.bof.model.db.Course;

public class StudentWithCourses {
    private IStudent student;
    private List<Course> courses;

    public StudentWithCourses(IStudent student, List<Course> courses) {
        this.student = student;
        this.courses = courses;
    }

    public IStudent getStudent() {
        return student;
    }

    public List<Course> getCourses() {
        return courses;
    }
}

