package edu.ucsd.cse110.bof.model;

import java.util.List;

import edu.ucsd.cse110.bof.model.db.Course;

public interface IStudent {
    String getName();
    String getPhotoUrl();
    List<Course> getCourses();
}
