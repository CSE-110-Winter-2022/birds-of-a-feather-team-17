package edu.ucsd.cse110.bof.model.db;

import android.content.Context;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.util.List;

import edu.ucsd.cse110.bof.model.IStudent;

@Entity(tableName = "students")
public class Student implements IStudent {

    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "student_id")
    public int studentId = 0;

    @ColumnInfo(name = "name")
    public String name;

    @ColumnInfo(name = "photo")
    public String photoURL;

    // number of BoFs (students with matching courses)
    @ColumnInfo(name = "matches")
    public int numMatches;

    // Student constructor
    public Student(String name, String photoURL) {
        this.name = name;
        this.photoURL = photoURL;
    }

    // Student default constructor
    public Student() {
        this.name = "Ava";
        this.photoURL = "ava.jpg";
        this.numMatches = 0;
    }

    // getters and setters
    public int getStudentId() {
        return studentId;
    }

    public void setStudentId(int studentId) {
        this.studentId = studentId;
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

    // pass in a context to receive singleton database instance
    public List<Course> getCourses(Context context) {
        AppDatabase db = AppDatabase.singleton(context);
        CoursesDao coursesDao = db.coursesDao();

        return coursesDao.getForStudent(this.studentId);
    }
}
