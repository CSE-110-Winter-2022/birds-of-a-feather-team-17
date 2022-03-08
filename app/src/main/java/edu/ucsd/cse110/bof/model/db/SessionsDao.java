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

    @Query("SELECT COUNT(*) from sessions")
    int count();

    @Insert
    void insert(Session session);
}
