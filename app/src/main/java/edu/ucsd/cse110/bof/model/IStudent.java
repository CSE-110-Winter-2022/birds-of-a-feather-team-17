package edu.ucsd.cse110.bof.model;

import java.util.List;

import edu.ucsd.cse110.bof.model.db.Course;

public interface IStudent {
    String getName();
    int getId();
    String getPhotoUrl();
    List<Course> getCourses();
}
