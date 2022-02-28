package edu.ucsd.cse110.bof.model.db;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Transaction;

import java.util.List;

@Dao
public interface CoursesDao {
    @Transaction
    @Query("SELECT * FROM courses where student_id=:studentId")
    List<Course> getForStudent(int studentId);

    @Query("SELECT * FROM courses WHERE course_id=:id")
    Course get(int id);

    @Query("SELECT COUNT(*) from courses")
    int count();

    @Query("SELECT MAX(course_id) from courses")
    int maxId();

    @Insert
    void insert(Course course);

    @Delete
    void delete(Course course);
}
