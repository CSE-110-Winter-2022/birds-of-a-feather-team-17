package edu.ucsd.cse110.bof.homepage;

import static java.lang.System.err;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Parcel;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.google.android.gms.nearby.Nearby;
import com.google.android.gms.nearby.messages.Message;
import com.google.android.gms.nearby.messages.MessageListener;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Observable;
import java.util.Observer;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicBoolean;

import edu.ucsd.cse110.bof.BoFsTracker;
import edu.ucsd.cse110.bof.FakedMessageListener;
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
    ToggleButton toggleSearch;
    Spinner p_spinner;
    RecyclerView studentsRecyclerView;
    RecyclerView.LayoutManager studentsLayoutManager;
    StudentsViewAdapter studentsViewAdapter;


    private static final String TAG = "HomePageReceiver";
    private MessageListener realListener;
    private FakedMessageListener fakedMessageListener;
    private MockedStudentFactory mockedStudentFactory;

    // Student received from listener(s) (if mockedStudent != null,
    // receivedStudentWithCourses will refer to same object as mockedStudent
    private StudentWithCourses receivedStudentWithCourses;

    //Student made on return from the NMMActivity
    private StudentWithCourses mockedStudent = null;
    private String mockCSV = null;

    private Context context;

    //for getting the csv from NMMActivity
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
                            String mockCSV = intent.getStringExtra("mockCSV");

                            mockedStudent = mockedStudentFactory.makeMockedStudent(mockCSV);
                            Log.d(TAG, "Mocked student " + mockedStudent.getStudent().getName() + " created");
                        }
                    }
                }
    });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_page);
        setTitle("Birds of a Feather");

        context = this;

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

        //set up listener for search button:
        toggleSearch = findViewById(R.id.search_button);
        toggleSearch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                onStartSearchingClicked();
            } else {
                onStopSearchingClicked();
            }
        });

        mockedStudentFactory = new MockedStudentFactory();

        realListener = new MessageListener() {

            @Override
            public void onFound(@NonNull Message message) {
                //make StudentWithCourses from byte array received
                Log.d(TAG, "found a (nonnull) message: "+message);
                ByteArrayInputStream bis =
                        new ByteArrayInputStream(message.getContent());
                ObjectInput stuObj;
                try {
                    stuObj = new ObjectInputStream(bis);
                    receivedStudentWithCourses =
                            (StudentWithCourses) stuObj.readObject();

                } catch (IOException | ClassNotFoundException e) {
                    e.printStackTrace();
                }

                Log.d(TAG, "message is a studentWithCourses named "
                        + receivedStudentWithCourses.getStudent().getName());

                updateLists();
            }
        };

        Log.d(TAG, "realListener created");
    }

    // creates the fakedMessageListener if a mockedStudent exists and
    // subscribes the realListener for actual bluetooth. Once the fML is
    // made, it should immediately call realListener's onFound(), which calls
    // updateList()
    public void onStartSearchingClicked() {
        //set up mock listener if a mockedStudent was made
        if (mockedStudent!=null) {
            Log.d(TAG, "mocked student found, subscribing fakedMessageListener");

            this.fakedMessageListener = new FakedMessageListener(this.realListener, 3,
                    mockedStudent, this);
            Nearby.getMessagesClient(this).subscribe(fakedMessageListener);
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

            //garbage collector will destroy current listener
            fakedMessageListener.stopRun();
            fakedMessageListener = null;
        }

        //actual listener (not necessary for project)
        Nearby.getMessagesClient(this).subscribe(realListener);
        Log.d(TAG, "unsubscribing realListener");
    }

    public void onGoToMockStudents(View view) {
        //make sure button is off when going to NMMActivity
        //button's listener will stop fakedMessageListener if needed
        toggleSearch.setChecked(false);

        Log.d(TAG, "going to NMM");
        Intent intent = new Intent(this, NearbyMessageMockActivity.class);

        activityLauncher.launch(intent);
    }

    public void onHistoryClicked(View view) {
        //make sure button is off when going to HistoryActivity
        //button's listener will stop fakedMessageListener if needed
        toggleSearch.setChecked(false);

        Log.d(TAG, "going to History");
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

    // called from listener, checks whether the student needs to be added to
    // homepage list and database
    public void updateLists()  {
        Student newStudent = mockedStudent.getStudent();
        String newName = newStudent.getName();

        //check that this student isn't in homepage list
        if (studentsViewAdapter.getStudents().contains(newStudent)) {
            Log.d(TAG, newName + " already in homepage list");
            return;
        }

        //use BoFsTracker to find common course
        ArrayList<Course> commonCourses = (ArrayList<Course>)
                BoFsTracker.getCommonCourses(
                        db.coursesDao().getForStudent(1),
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
            db.studentsDao().insert(newStudent);

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

        //resort the list
        Log.d(TAG, "student added, resorting the list...");
        studentsViewAdapter.sortList(p_spinner.getSelectedItem().toString());
        Log.d(TAG, "students list sorted");
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