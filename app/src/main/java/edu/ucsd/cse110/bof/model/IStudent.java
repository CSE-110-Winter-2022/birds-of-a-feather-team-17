package edu.ucsd.cse110.bof.model;

import android.content.Context;

import java.io.Serializable;
import java.util.List;

import edu.ucsd.cse110.bof.model.db.Course;

/**
 * Interface for a student
 */
public interface IStudent extends Serializable {
    int getStudentId();
    String getUUID();
    String getName();
    String getPhotoUrl();
    int getMatches();
    float getClassSizeWeight();
    int getRecencyWeight();
    boolean isWavedAtMe();
    boolean isWavedTo();
    int waveMultiplier();
    int favMultiplier();
    boolean getIsFav();

    void setUUID(String UUID);
    void setMatches(int numMatches);
    void setClassSizeWeight(float weight);
    void setRecencyWeight(int weight);
    void setWavedAtMe(boolean waved);
    void setWavedTo(boolean waved);
    void setIsFav(boolean isFav);
    void setWaveTarget(String waveTarget);
}
