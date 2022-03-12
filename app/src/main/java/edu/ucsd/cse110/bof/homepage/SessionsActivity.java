package edu.ucsd.cse110.bof.homepage;

import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import edu.ucsd.cse110.bof.R;
import edu.ucsd.cse110.bof.model.IStudent;
import edu.ucsd.cse110.bof.model.db.AppDatabase;
import edu.ucsd.cse110.bof.model.db.Session;
import edu.ucsd.cse110.bof.model.db.Student;

/**
 * Presenter for displaying all saved sessions (including favorites)
 */
public class SessionsActivity extends AppCompatActivity {
    private AppDatabase db;
    List<Session> sessions;

    private RecyclerView sessionsRecyclerView;
    private RecyclerView.LayoutManager sessionsLayoutManager;
    private SessionsViewAdapter sessionsViewAdapter;

    /**
     * Set up activity components
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_sessions);
        setTitle("Past Sessions");

        // Get sessions from database
        db = AppDatabase.singleton(this);
        sessions = db.sessionsDao().getAll();

        // Set up RecyclerView
        sessionsRecyclerView = findViewById(R.id.sessions_view);

        sessionsLayoutManager = new LinearLayoutManager(this);
        sessionsRecyclerView.setLayoutManager(sessionsLayoutManager);

        sessionsViewAdapter = new SessionsViewAdapter(sessions, db);
        sessionsRecyclerView.setAdapter(sessionsViewAdapter);
    }

    /**
     * Go back to home page when "Go back" is clicked
     * @param view required for onClickListener
     */
    public void onGoBackSessionsClicked(View view) {
        finish();
    }
}
