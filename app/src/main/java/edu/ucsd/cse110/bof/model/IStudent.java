package edu.ucsd.cse110.bof.model;

import android.content.Context;

import java.io.Serializable;
import java.util.List;

import edu.ucsd.cse110.bof.model.db.Course;

public interface IStudent extends Serializable {
    int getStudentId();
    String getName();
    String getPhotoUrl();
    List<Course> getCourses(Context context);
    int getMatches();
    float getClassSizeWeight();
    int getRecencyWeight();
    boolean getIsFav();

    void setMatches(int numMatches);
    void setClassSizeWeight(float weight);
    void setRecencyWeight(int weight);
    void setIsFav();
}
