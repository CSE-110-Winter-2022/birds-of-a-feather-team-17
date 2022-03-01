package edu.ucsd.cse110.bof.model.db;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.io.Serializable;
import java.util.Objects;

@Entity(tableName = "courses")
public class Course implements Serializable {

    @PrimaryKey
    @ColumnInfo(name = "course_id")
    public int courseId;

    @ColumnInfo(name = "student_id")
    public int studentId;

    public int year;
    public String quarter, subject, courseNum;
    public String courseSize;

    // Course constructor
    public Course(int courseId, int studentId, int year, String quarter, String subject, String courseNum, String courseSize) {
        this.courseId = courseId;
        this.studentId = studentId;
        this.year = year;
        this.quarter = quarter;
        this.subject = subject;
        this.courseNum = courseNum;
        this.courseSize = courseSize;
    }

    // getters and setters
    public int getCourseId() {
        return courseId;
    }

    public void setCourseId(int courseId) {
        this.courseId = courseId;
    }

    public int getStudentId() {
        return studentId;
    }

    public void setStudentId(int studentId) {
        this.studentId = studentId;
    }

    @NonNull
    @Override
    public String toString() {
        return ""+year+" "+quarter+" "+subject+" "+courseNum+" "+courseSize;
    }


    //bottom two methods required to check common courses, neither check ids
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Course)) return false;
        Course course = (Course) o;
        return year == course.year && Objects.equals(quarter, course.quarter) &&
                Objects.equals(subject, course.subject) &&
                Objects.equals(courseNum, course.courseNum) &&
                Objects.equals(courseSize, course.courseSize);
    }

    @Override
    public int hashCode() {
        return Objects.hash(year, quarter, subject, courseNum, courseSize);
    }
}
