package edu.ucsd.cse110.bof;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

public class NearbyMessageMockActivity extends AppCompatActivity {
    private static final String TAG = "MockingInputActivity";
    private EditText mockStudentInput;
    private String csv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_nearby_message_mock);

        mockStudentInput = findViewById(R.id.input_csv);
    }

    /**
     * Assumes that this input has valid csv, will create fake message
     * listener to mock a student being nearby (sends the student every 3
     * seconds)
     */
    public void onConfirmMockedStudent(View view) {
        csv = mockStudentInput.getText().toString();
        if (csv.equals("")) {
            Toast.makeText(this, "Please input csv", Toast.LENGTH_SHORT).show();
        }
        mockStudentInput.setText("");
    }

    public void onGoBackClicked(View view) {
        Intent intent = new Intent();
        intent.putExtra("mockCSV", csv);
        setResult(0, intent);

        NearbyMessageMockActivity.super.onBackPressed();
    }
}