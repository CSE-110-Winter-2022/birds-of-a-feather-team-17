package edu.ucsd.cse110.bof.homepage;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.os.Bundle;
import android.view.View;

import java.util.ArrayList;
import java.util.List;

import edu.ucsd.cse110.bof.R;
import edu.ucsd.cse110.bof.model.IStudent;
import edu.ucsd.cse110.bof.model.db.AppDatabase;
import edu.ucsd.cse110.bof.model.db.Student;
import edu.ucsd.cse110.bof.model.db.StudentsDao;

public class HistoryActivity extends AppCompatActivity {
    private AppDatabase db;
    private StudentsDao studentsDao;
    private Context context;

    List<Student> discoveredStudents;

    private RecyclerView studentsRecyclerView;
    private RecyclerView.LayoutManager studentsLayoutManager;
    private StudentsViewAdapter historyViewAdapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);

        context = this;

        db = AppDatabase.singleton(context);
        studentsDao = db.studentsDao();

        discoveredStudents = studentsDao.getAll();

        //remove self from list TODO: test
        discoveredStudents.remove(0);


        //set title
        setTitle("Discovered Students");

        //set up RecyclerView
        studentsRecyclerView = findViewById(R.id.history_view);

        studentsLayoutManager = new LinearLayoutManager(this);
        studentsRecyclerView.setLayoutManager(studentsLayoutManager);

        historyViewAdapter = new StudentsViewAdapter(discoveredStudents);
        studentsRecyclerView.setAdapter(historyViewAdapter);

    }

    public void onGoBackHistoryClicked(View view) {
        finish();
    }
}