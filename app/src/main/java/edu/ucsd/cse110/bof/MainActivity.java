package edu.ucsd.cse110.bof;

import androidx.appcompat.app.AppCompatActivity;


import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import edu.ucsd.cse110.bof.homepage.HomePageActivity;
import edu.ucsd.cse110.bof.login.LoginActivity;
import edu.ucsd.cse110.bof.login.NameActivity;
import edu.ucsd.cse110.bof.model.IStudent;
import edu.ucsd.cse110.bof.model.db.AppDatabase;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "StartPage";

    private AppDatabase db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        AppDatabase db = AppDatabase.singleton(this);
        IStudent user = db.studentsDao().get(1);
        if (user != null) {
            Log.d(TAG, "Found user " + user.getName() + ", going to HomePage");
            Intent intent = new Intent(this, HomePageActivity.class);
            this.startActivity(intent);
        }
        else {
            Log.d(TAG, "User not found, go to Login to start making new user");
            Intent intent = new Intent(this, LoginActivity.class);
            this.startActivity(intent);
        }
        finish();
    }
}