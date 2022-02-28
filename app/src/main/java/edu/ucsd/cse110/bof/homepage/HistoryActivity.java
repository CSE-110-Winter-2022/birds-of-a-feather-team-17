package edu.ucsd.cse110.bof.homepage;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.os.Bundle;
import android.view.View;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import edu.ucsd.cse110.bof.R;
import edu.ucsd.cse110.bof.model.IStudent;
import edu.ucsd.cse110.bof.model.db.AppDatabase;
import edu.ucsd.cse110.bof.model.db.Course;
import edu.ucsd.cse110.bof.model.db.Student;
import edu.ucsd.cse110.bof.model.db.StudentsDao;

public class HistoryActivity extends AppCompatActivity {
    private AppDatabase db;

    List<Student> discoveredStudents;

    private RecyclerView studentsRecyclerView;
    private RecyclerView.LayoutManager studentsLayoutManager;
    private StudentsViewAdapter historyViewAdapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);

        db = AppDatabase.singleton(this);

        //create list of discovered students (minus user (which has student_id=1))
        List<Student> tempList = db.studentsDao().getAll();
        int tempListSize = tempList.size();

        discoveredStudents = new ArrayList<>();
        for (int i=1; i<tempListSize; i++) {
            discoveredStudents.add(tempList.get(i));
        }
        //sort discoveredStudents by numMatches (greater has priority)
        discoveredStudents.sort((Comparator<IStudent>) (o1, o2) ->
                Integer.compare(o2.getMatches(), o1.getMatches()));

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