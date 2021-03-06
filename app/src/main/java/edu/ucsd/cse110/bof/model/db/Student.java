package edu.ucsd.cse110.bof.model.db;

import android.content.Context;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import edu.ucsd.cse110.bof.model.IStudent;

@Entity(tableName = "students")
public class Student implements IStudent {
    // Add constant for putting wavedAtMe and fav students on top while preserving sorting order
    public static final int WAVE_CONSTANT = 2000000;
    public static final int FAV_CONSTANT = 1000000;

    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "student_id")
    public int studentId = 0;

    @ColumnInfo(name = "UUID")
    public String UUID;

    @ColumnInfo(name = "name")
    public String name;

    @ColumnInfo(name = "photo")
    public String photoURL;

    // number of BoFs (students with matching courses)
    @ColumnInfo(name = "matches")
    public int numMatches;

    @ColumnInfo(name = "classSizeWeight")
    public float classSizeWeight;

    @ColumnInfo(name = "recencyWeight")
    public int recencyWeight;

    @ColumnInfo(name = "wavedAtMe")
    public boolean wavedAtMe;

    @ColumnInfo(name = "wavedTo")
    public boolean wavedTo;

    @ColumnInfo(name = "isFav")
    public boolean isFav;

    // Student constructor
    public Student(String name, String photoURL, String UUID) {
        this.name = name;
        this.photoURL = photoURL;
        this.UUID = UUID;
        this.numMatches = 0;
        this.classSizeWeight = 0;
        this.recencyWeight = 0;
        this.wavedAtMe = false;
        this.wavedTo = false;
        this.isFav = false;
    }

    // Student default constructor
    @Ignore
    public Student() {
        this.name = "Ava";
        this.photoURL = "ava.jpg";
        this.UUID = "a4ca50b6-941b-11ec-b909-0242ac120002";
        this.numMatches = 0;
        this.classSizeWeight = 0;
        this.recencyWeight = 0;
        this.wavedAtMe = false;
        this.wavedTo = false;
        this.isFav = false;
    }

    // Getters and setters
    public int getStudentId() {
        return studentId;
    }

    public void setStudentId(int studentId) {
        this.studentId = studentId;
    }

    public String getUUID() {
        return UUID;
    }

    public void setUUID(String UUID) {
        this.UUID = UUID;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPhotoUrl() { return photoURL; }

    public void setPhotoUrl(String photoURL) { this.photoURL = photoURL; }

    public int getMatches() { return numMatches; }

    public void setMatches(int numMatches) { this.numMatches = numMatches; }

    public float getClassSizeWeight() { return classSizeWeight; }

    public void setClassSizeWeight(float weight) { this.classSizeWeight = weight; }

    public int getRecencyWeight() { return recencyWeight; }

    public void setRecencyWeight(int weight) { this.recencyWeight = weight; }

    public boolean getIsFav() { return isFav; }

    public void setIsFav(boolean isFav) { this.isFav = isFav; }

    public boolean isWavedAtMe() {
        return wavedAtMe;
    }

    public void setWavedAtMe(boolean wavedAtMe) {
        this.wavedAtMe = wavedAtMe;
    }

    public boolean isWavedTo() {
        return wavedTo;
    }

    public void setWavedTo(boolean wavedTo) {
        this.wavedTo = wavedTo;
    }

    // Get addition multiplier to keep regular sorting order but with wave on top
    public int waveMultiplier() {
        if(wavedAtMe)
            return WAVE_CONSTANT;
        else
            return 0;
    }

    // Get addition multiplier to keep regular sorting order but fav provides higher weight
    public int favMultiplier() {
        if(isFav)
            return FAV_CONSTANT;
        else
            return 0;
    }

    // Pass in a context to receive singleton database instance
    public List<Course> getCourses(Context context) {
        AppDatabase db = AppDatabase.singleton(context);
        CoursesDao coursesDao = db.coursesDao();

        return coursesDao.getForStudent(this.studentId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, photoURL, UUID);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Student student = (Student) o;
        return name.equals(student.name) && photoURL.equals(student.photoURL) &&
                UUID.equals(student.UUID);
    }
}
