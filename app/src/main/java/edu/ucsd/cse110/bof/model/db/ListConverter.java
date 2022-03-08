package edu.ucsd.cse110.bof.model.db;

import androidx.room.TypeConverter;

import java.util.ArrayList;
import java.util.List;

//TODO: test
public class ListConverter {
    @TypeConverter
    public List<Integer> getListFromString(String studentIDs) {
        List<Integer> list = new ArrayList<>();

        String[] array = studentIDs.split(",");
        for (String s : array) {
            if (!s.isEmpty()) {
                list.add(Integer.parseInt(s));
            }
        }
        return list;
    }

    @TypeConverter
    public String getStringFromList(List<Integer> list) {
        StringBuilder studentIDs = new StringBuilder();
        int listSize = list.size();
        for (int i=0; i<listSize; i++) {
            if (i==0) { studentIDs.append(i); continue; }
            studentIDs.append(",").append(i);
        }
        return studentIDs.toString();
    }}