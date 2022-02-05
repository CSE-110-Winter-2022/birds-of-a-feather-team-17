package edu.ucsd.cse110.bof.model.db;

import androidx.room.Embedded;
import androidx.room.Relation;

import java.util.List;

import edu.ucsd.cse110.bof.model.IStudent;

public class StudentWithCourses implements IStudent {

    @Embedded
    public Student student;

    @Relation(parentColumn = "id",
            entityColumn = "student_id",
            entity = Course.class,
            projection = {"info"})
    public List<String> courses;

    @Override
    public String getName() {
        return this.student.name;
    }

    @Override
    public String getPhotoUrl() {
        return this.student.photoURL;
    }

    @Override
    public List<String> getCourses() {
        return this.courses;
    }
}
