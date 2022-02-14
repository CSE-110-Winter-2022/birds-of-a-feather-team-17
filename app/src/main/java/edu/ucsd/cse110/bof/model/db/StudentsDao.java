package edu.ucsd.cse110.bof.model.db;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Transaction;

import java.util.List;

@Dao
public interface StudentsDao {

    @Query("SELECT * FROM students")
    List<Student> getAll();

    @Query("SELECT * FROM students WHERE student_id=:id")
    Student get(int id);

    @Query("SELECT MAX(student_id) FROM students")
    int maxId();

    @Query("SELECT COUNT(*) from students")
    int count();

    @Insert
    void insert(Student student);

    @Delete
    void delete(Student student);
}
