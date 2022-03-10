package edu.ucsd.cse110.bof.model.db;

import androidx.room.TypeConverter;

import java.util.ArrayList;
import java.util.List;

//TODO: test
public class ListConverter {

    public static List<Integer> getListFromString(String studentIDs) {
        List<Integer> list = new ArrayList<>();

        String[] array = studentIDs.split(",");
        for (String s : array) {
            if (!s.isEmpty()) {
                list.add(Integer.parseInt(s));
            }
        }
        return list;
    }

    public static String getStringFromList(List<Integer> list) {
        StringBuilder studentIDs = new StringBuilder();
        int listSize = list.size();
        for (int i=0; i<listSize; i++) {
            if (i==0) { studentIDs.append(list.get(i)); continue; }
            studentIDs.append(",").append(list.get(i));
        }
        return studentIDs.toString();
    }
}