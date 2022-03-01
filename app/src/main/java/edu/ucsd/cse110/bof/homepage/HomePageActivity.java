package edu.ucsd.cse110.bof.homepage;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.google.android.gms.nearby.Nearby;
import com.google.android.gms.nearby.messages.Message;
import com.google.android.gms.nearby.messages.MessageListener;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.sql.SQLOutput;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import edu.ucsd.cse110.bof.BoFsTracker;
import edu.ucsd.cse110.bof.FakedMessageListener;
import edu.ucsd.cse110.bof.InputCourses.CoursesViewAdapter;
import edu.ucsd.cse110.bof.InputCourses.InputCourseActivity;
import edu.ucsd.cse110.bof.MockedStudentFactory;
import edu.ucsd.cse110.bof.NearbyMessageMockActivity;
import edu.ucsd.cse110.bof.R;
import edu.ucsd.cse110.bof.StudentWithCourses;
import edu.ucsd.cse110.bof.model.IStudent;
import edu.ucsd.cse110.bof.model.db.AppDatabase;
import edu.ucsd.cse110.bof.model.db.Course;
import edu.ucsd.cse110.bof.model.db.Student;

public class HomePageActivity extends AppCompatActivity {
    private AppDatabase db;
    private IStudent thisStudent;

    List<Student> myBoFs;

    RecyclerView studentsRecyclerView;
    RecyclerView.LayoutManager studentsLayoutManager;
    RecyclerView.AdapterDataObserver myObserver;
    StudentsViewAdapter studentsViewAdapter;

    private static final String TAG = "HomePageReceiver";
    private MessageListener realListener;
    private MessageListener fakedMessageListener;
    private MockedStudentFactory mockedStudentFactory;

    private StudentWithCourses mockedStudent = null;
    private String mockCSV = null;

    ActivityResultLauncher<Intent> activityLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    if (result.getResultCode() == 0) {
                        Log.d(TAG, "Back from NMM");
                        Intent intent = result.getData();

                        if (intent != null) {
                            Log.d(TAG, "Making mocked student from csv");
                            mockCSV = intent.getStringExtra("mockCSV");
                            mockedStudent = mockedStudentFactory.makeMockedStudent(mockCSV);
                            Log.d(TAG, "Mocked student created");
                        }
                    }
                }
    });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_page);
        setTitle("Birds of a Feather");

        //set thisStudent
        Intent intent = getIntent();
        db = AppDatabase.singleton(this);
        thisStudent = db.studentsDao().get(1);

        //set up RecyclerView
        myBoFs = new ArrayList<>();

        studentsRecyclerView = findViewById(R.id.students_view);

        studentsLayoutManager = new LinearLayoutManager(this);
        studentsRecyclerView.setLayoutManager(studentsLayoutManager);

        studentsViewAdapter = new StudentsViewAdapter(myBoFs);
        studentsRecyclerView.setAdapter(studentsViewAdapter);

        //TODO
        myObserver = new RecyclerView.AdapterDataObserver() {
            @Override
            public void onChanged() {
                studentsViewAdapter.getStudents().sort((Comparator<IStudent>) (o1, o2) -> Integer.compare(o2.getMatches(), o1.getMatches()));

                Log.d(TAG, "sorted students based on numMatches");
                super.onChanged();
            }
        };

        studentsViewAdapter.registerAdapterDataObserver(myObserver);


        //set up listener for search button:
        ToggleButton toggle = findViewById(R.id.search_button);
        toggle.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                onStartSearchingClicked();
            } else {
                onStopSearchingClicked();
            }
        });

        mockedStudentFactory = new MockedStudentFactory();

        realListener = new MessageListener() {
            StudentWithCourses receivedStudentWithCourses = null;

            @Override
            public void onFound(@NonNull Message message) {

                //make StudentWithCourses from byte array received
                Log.d(TAG, "found a (nonnull) message: "+message);
                ByteArrayInputStream bis =
                        new ByteArrayInputStream(message.getContent());
                ObjectInput stuObj = null;
                try {
                    stuObj = new ObjectInputStream(bis);
                    receivedStudentWithCourses =
                            (StudentWithCourses) stuObj.readObject();

                } catch (IOException | ClassNotFoundException e) {
                    e.printStackTrace();
                }

                Log.d(TAG, "message is a studentWithCourses named "
                        + receivedStudentWithCourses.getStudent().getName());

                updateLists(receivedStudentWithCourses);
            }
        };

        Log.d(TAG, "realListener created");
    }

    public void onStartSearchingClicked() {
        //set up mock listener if a mockedStudent was made
        if (mockedStudent!=null) {
            this.fakedMessageListener = new FakedMessageListener(this.realListener, 3,
                    mockedStudent);
            Nearby.getMessagesClient(this).subscribe(fakedMessageListener);

            Log.d(TAG, "mocked student found, subscribing fakedMessageListener");
        }
        else {
            Log.d(TAG, "Not mocking student");
            Toast.makeText(this, "Not mocking student", Toast.LENGTH_SHORT).show();
        }

        //actual listener (not necessary for project)
        Nearby.getMessagesClient(this).subscribe(realListener);
        Log.d(TAG, "subscribing realListener");
    }

    public void onStopSearchingClicked() {
        if (fakedMessageListener != null) {
            Nearby.getMessagesClient(this).unsubscribe(fakedMessageListener);
            Log.d(TAG, "destroying fakedMessageListener");

            //garbage collector will destroy current listener?
            fakedMessageListener = null;
        }

        //actual listener (not necessary for project)
        Nearby.getMessagesClient(this).subscribe(realListener);
        Log.d(TAG, "unsubscribing realListener");
    }

    public void onGoToMockStudents(View view) {
        //stop searching when going to NMM activity
        onStopSearchingClicked();

        Intent intent = new Intent(this, NearbyMessageMockActivity.class);

        activityLauncher.launch(intent);
    }

    public void onHistoryClicked(View view) {
        if (fakedMessageListener != null) {
            Nearby.getMessagesClient(this).unsubscribe(fakedMessageListener);
            Log.d(TAG, "going to History, unsubscribing fakedMessageListener");
        }
        Intent intent = new Intent(this, HistoryActivity.class);
        startActivity(intent);
    }

    public void onAddClassesClicked(View view) {
        Intent intent = new Intent(this, InputCourseActivity.class);

        //check if navigated from HomePageActivity or not (as opposed to PhotoActivity)
        intent.putExtra("onHomePage",true);
        startActivity(intent);
    }

    // called from listener, checks whether the student needs to be added to
    // homepage list and database
    public void updateLists(StudentWithCourses receivedStudentWithCourses) {
        Student newStudent = receivedStudentWithCourses.getStudent();
        String newName = newStudent.getName();

        //check that this student isn't in homepage list
        if (studentsViewAdapter.getStudents().contains(newStudent)) {
            Log.d(TAG, newName + " already in homepage list");
            return;
        }

        //use BoFsTracker to find common course
        ArrayList<Course> commonCourses = (ArrayList<Course>)
                BoFsTracker.getCommonCourses(
                        thisStudent.getCourses(getApplicationContext()),
                        receivedStudentWithCourses.getCourses());

        //if at least one common course, add this student to list of students
        //and the database
        if (commonCourses.size() == 0) {
            Log.d(TAG, newName + " has no common courses");
            return;
        }

        //set matches to add into list
        newStudent.setMatches(commonCourses.size());

        //add this student to viewAdapter list
        Log.d(TAG, "preparing to add " + newName + " to recycler view");
        studentsViewAdapter.addStudent(receivedStudentWithCourses.getStudent());

        //only add to db if not already in db
        if (!db.studentsDao().getAll().contains(newStudent)) {
            Log.d(TAG,newName + " will be added to database");
            db.studentsDao().insert((Student) receivedStudentWithCourses.getStudent());

            int insertedId = db.studentsDao().maxId();
            newStudent.setStudentId(insertedId);
            int insertedCourseId = db.coursesDao().maxId();

            //only common courses need to be added to db
            for (Course receivedCourse : commonCourses) {

                receivedCourse.setStudentId(insertedId);
                receivedCourse.setCourseId(++insertedCourseId);
                db.coursesDao().insert(receivedCourse);
            }
        }
        else {
            Log.d(TAG,newName + " already in database");
        }

        System.out.println("wtf");
    }

    //for testing, need to be able to make mocked student without going to NMM
    public void setMockedStudent(StudentWithCourses stuWithCourses) {
        mockedStudent = stuWithCourses;
    }

    //for testing, need to switch working database with a test db
    public void setDb(AppDatabase db) {
        this.db = db;
    }

    //for testing, need to get a reference to the studentsViewAdapter to see
    //if it mocked students added correctly, sorted
    public StudentsViewAdapter getStudentsViewAdapter() {
        return this.studentsViewAdapter;
    }
}