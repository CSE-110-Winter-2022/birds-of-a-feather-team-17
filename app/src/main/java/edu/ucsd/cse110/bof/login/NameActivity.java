package edu.ucsd.cse110.bof.login;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import edu.ucsd.cse110.bof.R;

/**
 * Handles user's name entry for their profile
 */
public class NameActivity extends AppCompatActivity {
    private String username;
    private EditText usernameInput;

    /**
     * Sets up components for name entry
     * @param savedInstanceState required for onCreate
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_name);

        usernameInput = findViewById(R.id.editName);

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            username = extras.getString("name");
        }
        usernameInput.setText(username); // Prefill username with Google Login Info
    }

    /**
     * Checks the user's entry and stores the name upon confirmation
     * @param view required for onClickListener
     */
    public void confirmName(View view) {
        username = usernameInput.getText().toString();

        // No-blank string validity
        if (username.equals("")) {
            Toast.makeText(this,"Invalid name", Toast.LENGTH_SHORT).show();
            return;
        }

        Intent intent = new Intent(this, PhotoActivity.class);
        intent.putExtra("student_name", username);
        startActivity(intent);
        finish();
    }
}