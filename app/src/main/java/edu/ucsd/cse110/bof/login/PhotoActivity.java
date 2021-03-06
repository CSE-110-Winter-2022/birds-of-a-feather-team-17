package edu.ucsd.cse110.bof.login;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;

import edu.ucsd.cse110.bof.InputCourses.InputCourseActivity;
import edu.ucsd.cse110.bof.R;
import edu.ucsd.cse110.bof.model.db.AppDatabase;
import edu.ucsd.cse110.bof.model.db.Session;
import edu.ucsd.cse110.bof.model.db.Student;

import android.webkit.URLUtil;
import android.widget.Toast;

import java.util.UUID;

public class PhotoActivity extends AppCompatActivity {
    private String photoURL;
    private EditText photoInput;
    private String username;

    private AppDatabase db;

    private static final String TAG = "PhotoActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_photo);

        db = AppDatabase.singleton(this);

        photoInput = (EditText)findViewById(R.id.editPhotoURL);

        //Retrieve username sent from NameActivity
        Bundle extras = getIntent().getExtras();
        username = extras.getString("student_name");

        Log.d(TAG, "Received user's name: " + username);
    }

    public void submitPhoto(View view) {
        //use default if user leaves input empty
        if (photoInput.getText().toString().equals("")) {
            photoURL = "https://lh3.googleusercontent.com/pw/AM-JKLXQ2ix4dg-PzLrPOSMOOy6M3PSUrijov9jCLXs4IGSTwN73B4kr-F6Nti_4KsiUU8LzDSGPSWNKnFdKIPqCQ2dFTRbARsW76pevHPBzc51nceZDZrMPmDfAYyI4XNOnPrZarGlLLUZW9wal6j-z9uA6WQ=w854-h924-no?authuser=0";
        }
        else {
            photoURL = photoInput.getText().toString();

            if (!URLUtil.isValidUrl(photoURL)) {
                Toast.makeText(this, "Invalid URL", Toast.LENGTH_SHORT).show();
                return;
            }
        }

        Log.d(TAG, "Received user's photoURL: " + photoURL);

        //Generate a random UUID for the user
        String uuid = UUID.randomUUID().toString();

        //insert user into database (student_id=1, first element in database)
        db.studentsDao().insert(new Student(username, photoURL, uuid));

        //create a new session called Favorites
        db.sessionsDao().insert(new Session("","","Favorites"));

        //Link to InputCourseActivity
        Intent intent = new Intent(this, InputCourseActivity.class);

        //check if navigated from HomePageActivity or not
        intent.putExtra("onHomePage",false);
        startActivity(intent);
        finish();

    }
}