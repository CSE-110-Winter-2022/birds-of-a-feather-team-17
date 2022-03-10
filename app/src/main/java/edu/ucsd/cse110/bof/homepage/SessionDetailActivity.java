package edu.ucsd.cse110.bof.homepage;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import edu.ucsd.cse110.bof.R;
import edu.ucsd.cse110.bof.model.IStudent;
import edu.ucsd.cse110.bof.model.db.AppDatabase;
import edu.ucsd.cse110.bof.model.db.Course;
import edu.ucsd.cse110.bof.model.db.Session;
import edu.ucsd.cse110.bof.model.db.Student;
import edu.ucsd.cse110.bof.model.db.StudentsDao;

public class SessionDetailActivity extends AppCompatActivity {
    private static final String TAG = "SessionDetailActivityLog";

    private AppDatabase db;

    List<Student> discoveredStudents;

    private RecyclerView studentsRecyclerView;
    private RecyclerView.LayoutManager studentsLayoutManager;
    private StudentsViewAdapter studentsViewAdapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);
        setTitle("Students in Session: ");

        Spinner p_spinner = findViewById(R.id.priority_spinner2);
        ArrayAdapter<CharSequence> p_adapter = ArrayAdapter.createFromResource(this, R.array.priorities_array, android.R.layout.simple_spinner_item);
        p_adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        p_spinner.setAdapter(p_adapter);

        //Retrieve session_id sent from NameActivity
        Bundle extras = getIntent().getExtras();
        int session_id = extras.getInt("session_id");

        db = AppDatabase.singleton(this);
        Session session = db.sessionsDao().get(session_id);
        List<Integer> studentIDList = session.getStudentList();

        Log.d(TAG, "Received Session id: " + session_id + " with name: " + session.dispName);

        discoveredStudents = new ArrayList<>();
        for (int id : studentIDList) {
            discoveredStudents.add(db.studentsDao().get(id));
        }

        //set up RecyclerView
        studentsRecyclerView = findViewById(R.id.history_view);

        studentsLayoutManager = new LinearLayoutManager(this);
        studentsRecyclerView.setLayoutManager(studentsLayoutManager);

        studentsViewAdapter = new StudentsViewAdapter(discoveredStudents);
        studentsViewAdapter.setContext(this);

        studentsRecyclerView.setAdapter(studentsViewAdapter);

        p_spinner.setOnItemSelectedListener(
                new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {

                        Log.d(TAG, "Selecting priority...");
                        String priority = parent.getItemAtPosition(pos).toString();
                        studentsViewAdapter.sortList(priority);
                        Log.d(TAG, "List sorted based on priority: "+priority);

                    }
                    @Override
                    public void onNothingSelected(AdapterView<?> parent) {
                    }
                });

    }

    public void onGoBackHistoryClicked(View view) {
        finish();
    }
}