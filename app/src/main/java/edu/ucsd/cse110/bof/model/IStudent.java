package edu.ucsd.cse110.bof.model;

import android.content.Context;

import java.io.Serializable;
import java.util.List;

import edu.ucsd.cse110.bof.model.db.Course;

public interface IStudent extends Serializable {
    int getStudentId();
    int getMatches();
    String getName();
    String getPhotoUrl();
    List<Course> getCourses(Context context);
    int getMatches();

    void setMatches(int size);
}
