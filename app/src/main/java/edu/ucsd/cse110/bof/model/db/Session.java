package edu.ucsd.cse110.bof.model.db;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.room.TypeConverter;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import edu.ucsd.cse110.bof.model.db.Student;

@Entity(tableName = "sessions")
public class Session {

    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "session_id")
    public int sessionId = 0;

    @ColumnInfo(name = "student_list")
    public List<Integer> studentIDList;

    //internal identifier for creation time
    @ColumnInfo(name = "creation_time")
    public String creationTime;

    @ColumnInfo(name = "display_name")
    public String dispName;

    // Course constructor
    public Session(List<Integer> studentIDList, String creationTime, String dispName) {
        this.studentIDList = studentIDList;
        this.creationTime = creationTime;
        this.dispName = dispName;
    }

    //Set display name
    public void setDispName(String dispName) {
        this.dispName = dispName;
    }

    //add a student by adding to studentsIDList, then calling method to update stringJSON list
//    public void addStudent(Integer studID){
//        studentIDList.add(studID);
//        //updateJSONList();
//    }

    // getters
    public int getSessionID(){
        return sessionId;
    }

    public List<Integer> getStudentList() {
        return studentIDList;
    }

    //Handles Name/creation time
    //If display name has not be set, display the creation time as the session name
    public String toString(){
        if( dispName.equals("") ){
            return creationTime;
        }
        else{
            return dispName;
        }
    }
}


