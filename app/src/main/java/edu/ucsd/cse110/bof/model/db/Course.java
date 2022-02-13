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
    @ColumnInfo(name = "id")
    public int courseId;

    @ColumnInfo(name = "student_id")
    public int studentId;

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


    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Course)) {
            return false;
        }

        Course other = (Course) o;

        //should not check database ids when equality checking
        return (other.year == this.year)
                && (other.quarter.equals(this.quarter))
                && (other.subject.equals(this.subject))
                && (other.courseNum.equals(this.courseNum));
    }
}
