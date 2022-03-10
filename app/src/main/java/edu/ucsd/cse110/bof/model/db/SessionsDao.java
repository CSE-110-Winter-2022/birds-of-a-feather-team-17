package edu.ucsd.cse110.bof.model.db;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

import edu.ucsd.cse110.bof.model.db.Session;

@Dao
public interface SessionsDao {

    @Query("SELECT * FROM sessions")
    List<Session> getAll();

    @Query("SELECT * FROM sessions WHERE session_id=:id")
    Session get(int id);

    @Query("SELECT MAX(session_id) FROM sessions")
    int maxId();

    @Query("SELECT COUNT(*) from sessions")
    int count();

    @Insert
    void insert(Session session);

    @Query("UPDATE sessions SET student_list=:updatedList WHERE session_id=:id")
    void updateStudentList(int id, String updatedList);

    @Query("UPDATE sessions SET display_name=:updatedName WHERE session_id=:id")
    void updateDispName(int id, String updatedName);
}
