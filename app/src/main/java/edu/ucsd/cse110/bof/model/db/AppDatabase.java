package edu.ucsd.cse110.bof.model.db;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

@Database(entities = {Student.class, Course.class}, version = 1)
public abstract class AppDatabase extends RoomDatabase {
    private static AppDatabase singletonInstance;

    public static AppDatabase singleton(Context context) {
        if (singletonInstance == null) {
//            singletonInstance = Room.databaseBuilder(context, AppDatabase.class, "students.db")
//                    .createFromAsset("starter-persons.db")
//                    .allowMainThreadQueries()
//                    .build();
            singletonInstance = Room.databaseBuilder(context, AppDatabase.class, "students.db")
                    .allowMainThreadQueries()
                    .build();
        }

        return singletonInstance;
    }

    public static AppDatabase useTestSingleton(Context context) {
        singletonInstance = Room.inMemoryDatabaseBuilder(context, AppDatabase.class)
                .allowMainThreadQueries()
                .build();
        return singletonInstance;
    }

    public abstract StudentsDao studentsDao();
    public abstract CoursesDao coursesDao();
}
