package edu.ucsd.cse110.bof.login;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;

import edu.ucsd.cse110.bof.InputCourses.InputCourseActivity;
import edu.ucsd.cse110.bof.R;

public class PhotoActivity extends AppCompatActivity {
    private String photoURL;
    private EditText photoInput;
    private String username;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_photo);

        photoInput = (EditText)findViewById(R.id.editPhotoURL);

        //Retrieve username sent from NameActivity
        Bundle extras = getIntent().getExtras();
        username = extras.getString("name");
    }

    public void submitPhoto(View view) {

        photoURL = photoInput.getText().toString();

        //Link to InputCourseActivity
        Intent intent = new Intent(this, InputCourseActivity.class);
        startActivity(intent);

    }
}