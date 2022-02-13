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
import edu.ucsd.cse110.bof.model.db.Student;

import android.webkit.URLUtil;
import android.widget.Toast;

public class PhotoActivity extends AppCompatActivity {
    private String photoURL;
    private EditText photoInput;
    private String username;
    private static final String TAG = "PhotoActivity";

    private AppDatabase db;

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
            photoURL = getResources().getString(R.string.default_photo_url);
        }
        else {
            photoURL = photoInput.getText().toString();

            //TODO: test
            if (!URLUtil.isValidUrl(photoURL)) {
                Toast.makeText(this, "Invalid URL", Toast.LENGTH_SHORT).show();
                return;
            }
        }

        //insert user into database (student_id=1, first element in database)
        db.studentsDao().insert(new Student(username, photoURL));

        //Link to InputCourseActivity
        Intent intent = new Intent(this, InputCourseActivity.class);
        startActivity(intent);
        finish();

    }
}