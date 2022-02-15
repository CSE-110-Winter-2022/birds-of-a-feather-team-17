package edu.ucsd.cse110.bof;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.nfc.Tag;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;

import com.google.android.gms.nearby.Nearby;
import com.google.android.gms.nearby.messages.Message;
import com.google.android.gms.nearby.messages.MessageListener;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.Scanner;

import edu.ucsd.cse110.bof.homepage.HomePageActivity;
import edu.ucsd.cse110.bof.model.IStudent;
import edu.ucsd.cse110.bof.model.db.Course;
import edu.ucsd.cse110.bof.model.db.Student;
import edu.ucsd.cse110.bof.StudentWithCourses;

public class NearbyMessageMockActivity extends AppCompatActivity {
    private static final String TAG = "MockingReceiver";
    private MessageListener realListener;
    private EditText mockStudentInput;
    private MockedStudentFactory mockedStudentFactory;
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
        mockStudentInput.setText("");
    }

    public void onGoBackClicked(View view) {
        Intent intent = new Intent();
        intent.putExtra("mockCSV", csv);
        setResult(0, intent);

        NearbyMessageMockActivity.super.onBackPressed();
    }
}