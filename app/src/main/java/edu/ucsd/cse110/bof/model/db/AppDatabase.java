package edu.ucsd.cse110.bof.model.db;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

/**
 * Implementation of the Room API
 */
@Database(entities = {Student.class, Course.class, Session.class}, version = 1)
public abstract class AppDatabase extends RoomDatabase {
    private static AppDatabase singletonInstance;

    /**
     * Create a singleton pattern database
     * @param context application context
     * @return the database made
     */
    public static AppDatabase singleton(Context context) {
        if (singletonInstance == null) {
            singletonInstance = Room.databaseBuilder(context, AppDatabase.class, "students.db")
                    .allowMainThreadQueries()
                    .build();
        }

        return singletonInstance;
    }

    /**
     * Create a singleton pattern testing database
     * @param context application context
     * @return the database made
     */
    public static AppDatabase useTestSingleton(Context context) {
        singletonInstance = Room.inMemoryDatabaseBuilder(context, AppDatabase.class)
                .allowMainThreadQueries()
                .build();
        return singletonInstance;
    }

    public abstract StudentsDao studentsDao();
    public abstract CoursesDao coursesDao();
    public abstract SessionsDao sessionsDao();
}
