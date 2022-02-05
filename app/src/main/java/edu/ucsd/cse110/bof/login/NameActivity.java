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

        usernameInput = (EditText)findViewById(R.id.editName);
    }

    public void confirmName(View view) {

        username = usernameInput.getText().toString();

        //TODO: Save the name

        Intent intent = new Intent(this, PhotoActivity.class); //link to PhotoActivity

        //Might need this for future
        //intent.putExtra("name", "yadayada");

        startActivity(intent);
    }
}