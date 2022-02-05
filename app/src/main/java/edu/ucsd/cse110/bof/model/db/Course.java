package edu.ucsd.cse110.bof.model.db;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "courses")
public class Course {

    @PrimaryKey
    @ColumnInfo(name = "id")
    public int courseId;

    @ColumnInfo(name = "student_id")
    public int studentId;

//    @ColumnInfo(name = "info")
//    public String info;

    public int year;
    String quarter, subject, courseNum;

    public Course(int courseId, int studentId, int year, String quarter, String subject, String courseNum) {
        this.courseId = courseId;
        this.studentId = studentId;
        this.year = year;
        this.quarter = quarter;
        this.subject = subject;
        this.courseNum = courseNum;
    }

    @NonNull
    @Override
    public String toString() {
        return ""+year+" "+quarter+" "+subject+" "+courseNum;
    }
}
