package edu.ucsd.cse110.bof.homepage;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import java.util.ArrayList;
import java.util.List;

import edu.ucsd.cse110.bof.R;
import edu.ucsd.cse110.bof.model.db.AppDatabase;
import edu.ucsd.cse110.bof.model.db.Session;
import edu.ucsd.cse110.bof.model.db.Student;

/**
 * Allows viewing of a session's saved students
 */
public class SessionDetailActivity extends AppCompatActivity {
    private static final String TAG = "SessionDetailActivityLog";

    private AppDatabase db;

    List<Student> discoveredStudents;

    private RecyclerView studentsRecyclerView;
    private RecyclerView.LayoutManager studentsLayoutManager;
    private StudentsViewAdapter studentsViewAdapter;


    /**
     * Gathers all the students from a session using DB, populates them into a StudentsViewAdapter
     * @param savedInstanceState required for onCreate
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);

        Spinner p_spinner = findViewById(R.id.priority_spinner2);
        ArrayAdapter<CharSequence> p_adapter = ArrayAdapter.createFromResource(this, R.array.priorities_array, android.R.layout.simple_spinner_item);
        p_adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        p_spinner.setAdapter(p_adapter);

        // Retrieve session_id sent from NameActivity
        Bundle extras = getIntent().getExtras();
        int session_id = extras.getInt("session_id");

        db = AppDatabase.singleton(this);
        Session session = db.sessionsDao().get(session_id);
        List<Integer> studentIDList = session.getStudentList();

        setTitle("Students in Session: " + session.dispName);

        Log.d(TAG, "Received Session id: " + session_id + " with name: " + session.dispName);

        discoveredStudents = new ArrayList<>();
        for (int id : studentIDList) {
            discoveredStudents.add(db.studentsDao().get(id));
        }

        // Set up RecyclerView
        studentsRecyclerView = findViewById(R.id.history_view);

        studentsLayoutManager = new LinearLayoutManager(this);
        studentsRecyclerView.setLayoutManager(studentsLayoutManager);

        studentsViewAdapter = new StudentsViewAdapter(discoveredStudents);
        studentsViewAdapter.setContext(this);

        studentsRecyclerView.setAdapter(studentsViewAdapter);

        // Allows sorting by priority in a session
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

    /**
     * Go back to the list of sessions upon user clicking to "Go Back"
     * @param view required for onClickListeners
     */
    public void onGoBackHistoryClicked(View view) {
        finish();
    }
}