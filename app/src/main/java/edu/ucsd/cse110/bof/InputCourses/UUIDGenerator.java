package edu.ucsd.cse110.bof.InputCourses;

import static android.content.Context.MODE_PRIVATE;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.UUID;

/**
 * Generates a randomized UUID, stores it in SharedPreferences
 */
public class UUIDGenerator
{
    public static void generate(Context context)
    {
        SharedPreferences sharedPreferences = context.getSharedPreferences("BoF",MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("UUID", UUID.randomUUID().toString());
        editor.apply();
    }
}
