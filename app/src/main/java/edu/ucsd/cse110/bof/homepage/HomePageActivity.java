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
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Spinner;
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
    StudentsViewAdapter studentsViewAdapter;

    Spinner p_spinner;          //spinner (drop-down menu) for priority sorting

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

        //create spinner (drop-down menu) for priorities/sorting algorithms
        p_spinner = findViewById(R.id.priority_spinner);
        ArrayAdapter<CharSequence> p_adapter = ArrayAdapter.createFromResource(this, R.array.priorities_array, android.R.layout.simple_spinner_item);
        p_adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        p_spinner.setAdapter(p_adapter);

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

        //set up listener for search button:
        ToggleButton toggle = findViewById(R.id.search_button);
        toggle.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                onStartSearchingClicked();
            } else {
                onStopSearchingClicked();
            }
        });

        p_spinner.setOnItemSelectedListener(
                new AdapterView.OnItemSelectedListener() {
                    public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {

                        String priority = parent.getItemAtPosition(pos).toString();
                        studentsViewAdapter.sortList(priority);

                    }
                    public void onNothingSelected(AdapterView<?> parent) {
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

                List<Student> dbStudents = db.studentsDao().getAll();

                Log.d(TAG,
                        "message is a studentWithCourses named "
                                + receivedStudentWithCourses.getStudent().getName());

                //check that this student isn't in list nor in database
                if (!myBoFs.contains(receivedStudentWithCourses.getStudent())) {
                    Log.d(TAG, "student not in homepage list nor database");

                    //use BoFsTracker to find common classes
                    ArrayList<Course> commonCourses = (ArrayList<Course>)
                            BoFsTracker.getCommonCourses(
                                    thisStudent.getCourses(getApplicationContext()),
                                    receivedStudentWithCourses.getCourses());

                    //if not empty list, add this student to list of students
                    //and the database
                    if (commonCourses.size() != 0) {
                        Log.d(TAG,"studentWithCourses has a common class");

                        //add this student to viewAdapter list
                        receivedStudentWithCourses.getStudent().setMatches(commonCourses.size());

                        receivedStudentWithCourses.getStudent().setClassSizeWeight(calcClassSizeWeight(commonCourses));

                        receivedStudentWithCourses.getStudent().setRecencyWeight(calcRecencyWeight(commonCourses));


                        receivedStudentWithCourses.setCourses(commonCourses);

                        //only add to db if not already in db
                        if (!dbStudents.contains((Student) receivedStudentWithCourses.getStudent())) {
                            db.studentsDao().insert((Student) receivedStudentWithCourses.getStudent());

                            int insertedId = db.studentsDao().maxId();
                            ((Student) receivedStudentWithCourses.getStudent()).setStudentId(insertedId);
                            int insertedCourseId = db.coursesDao().maxId();

                            //only common courses need to be added to db
                            for (Course receivedCourse : commonCourses) {

                                receivedCourse.setStudentId(insertedId);
                                receivedCourse.setCourseId(++insertedCourseId);
                                db.coursesDao().insert(receivedCourse);
                            }
                        }

                        Log.d(TAG, "preparing to add new mocked student to recycler view");

                        studentsViewAdapter.addStudent(receivedStudentWithCourses.getStudent());

                        //resort the list
                        Log.d(TAG, "student added, resorting the list...");
                        studentsViewAdapter.sortList(p_spinner.getSelectedItem().toString());

                    }
                }
            }
        };

        Log.d(TAG, "realListener created");
    }

    public void onStartSearchingClicked() {
        //set up mock listener for receiving mocked items
        if (mockedStudent!=null) {
            this.fakedMessageListener = new FakedMessageListener(this.realListener, 3,
                    mockedStudent);
            Nearby.getMessagesClient(this).subscribe(fakedMessageListener);

            Log.d(TAG, "mocked student found, subscribing fakedMessageListener");
        }
        else {
            Log.d(TAG, "No students found/mocked");
            Toast.makeText(this, "No students found", Toast.LENGTH_SHORT).show();
        }
    }

    public void onStopSearchingClicked() {
        if (fakedMessageListener != null) {
            Nearby.getMessagesClient(this).unsubscribe(fakedMessageListener);
            Log.d(TAG, "stopped searching, unsubscribing fakedMessageListener");
        }
    }

    public void onGoToMockStudents(View view) {
        if (fakedMessageListener != null) {
            Nearby.getMessagesClient(this).unsubscribe(fakedMessageListener);
            Log.d(TAG, "going to MockStudents, unsubscribing fakedMessageListener");
        }
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

    public static float calcClassSizeWeight(ArrayList<Course> courses) {
        if(courses == null) { return 0; }
        float sum = 0;
        for(Course c : courses) {
            switch(c.courseSize) {
                case "Tiny": sum += 1.00; break;
                case "Small": sum += 0.33; break;
                case "Medium": sum += 0.18; break;
                case "Large": sum += 0.10; break;
                case "Huge": sum += 0.06; break;
                case "Gigantic": sum += 0.01; break;
            }
        }
        return sum;
    }

    public static int calcRecencyWeight(ArrayList<Course> courses) {
        if(courses == null) { return 0; }
        int sum = 0;
        for(Course c : courses) {
            if(2022 - c.year > 1) {
                sum += 1;
            } else {
                switch (c.quarter) {
                    case "FA": sum += 5; break;
                    case "SP": sum += 3; break;
                    case "WI": sum += 2; break;
                    default: sum += 4;
                }
            }
        }
        return sum;
    }
}