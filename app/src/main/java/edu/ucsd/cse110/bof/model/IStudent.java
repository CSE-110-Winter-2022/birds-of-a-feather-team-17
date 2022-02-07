package edu.ucsd.cse110.bof.model;

import java.io.Serializable;
import java.util.List;

import edu.ucsd.cse110.bof.model.db.Course;

public interface IStudent extends Serializable {
    int getId();
    String getName();
    String getPhotoUrl();
    List<Course> getCourses();
}
