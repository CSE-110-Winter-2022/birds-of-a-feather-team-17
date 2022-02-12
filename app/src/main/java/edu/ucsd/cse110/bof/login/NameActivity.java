package edu.ucsd.cse110.bof.login;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;

import edu.ucsd.cse110.bof.R;

public class NameActivity extends AppCompatActivity {
    private String username;
    private EditText usernameInput;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_name);

        usernameInput = findViewById(R.id.editName);

        Bundle extras = getIntent().getExtras();
        username = extras.getString("name");

        usernameInput.setText(username); //Prefill username with Google Login Info
    }

    public void confirmName(View view) {
        username = usernameInput.getText().toString();

        Intent intent = new Intent(this, PhotoActivity.class); //link to PhotoActivity
        intent.putExtra("student_name", username);
        startActivity(intent);
    }
}