package edu.ucsd.cse110.bof;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;

import edu.ucsd.cse110.bof.login.LoginActivity;
import edu.ucsd.cse110.bof.login.NameActivity;
import edu.ucsd.cse110.bof.model.db.AppDatabase;

public class MainActivity extends AppCompatActivity {

    private AppDatabase db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        db = AppDatabase.singleton(this);

        System.out.println("Student count "+ db.studentsDao().count());

        // Should check if the student is made and, if so, move
        // directly to homepage
        if (db.studentsDao().count() > 0) {
            Intent intent = new Intent(this, HomePageActivity.class);
            this.startActivity(intent);
        }

        else {
            //Test code for starting in Login Activity
            Intent intent = new Intent(this, LoginActivity.class);
            this.startActivity(intent);
        }
        finish();
    }
}