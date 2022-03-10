package edu.ucsd.cse110.bof.homepage;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
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
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import edu.ucsd.cse110.bof.BoFsTracker;
import edu.ucsd.cse110.bof.FakedMessageListener;
import edu.ucsd.cse110.bof.IBuilder;
import edu.ucsd.cse110.bof.InputCourses.InputCourseActivity;
import edu.ucsd.cse110.bof.NearbyMessageMockActivity;
import edu.ucsd.cse110.bof.R;
import edu.ucsd.cse110.bof.model.StudentWithCourses;
import edu.ucsd.cse110.bof.StudentWithCoursesBuilder;
import edu.ucsd.cse110.bof.model.db.AppDatabase;
import edu.ucsd.cse110.bof.model.db.Course;
import edu.ucsd.cse110.bof.model.db.Session;
import edu.ucsd.cse110.bof.model.db.Student;

public class HomePageActivity extends AppCompatActivity {
    private AppDatabase db;                     //database
    private Student thisStudent;                //user
    private List<Course> thisStudentCourses;    //user's courses
    private Message selfMessage;                //a message containing this user
    private StudentWithCourses selfStudentWithCourses; //SWC of user

    Session session = null;                     //current search session
    int sessionId;                              //session id in database
    private Date currDate = null;               //for setting session title
    @SuppressLint("ConstantLocale")
    private static final SimpleDateFormat sdf =
            new SimpleDateFormat("MM/dd/yy hh:mmaa", Locale.getDefault());

    ToggleButton toggleSearch;                  //ref to search button
    Spinner p_spinner;                          //ref to priority spinner
    RecyclerView studentsRecyclerView;          //recyclerView containing BoFs
    RecyclerView.LayoutManager studentsLayoutManager;   //for use with
                                                        // recyclerView
    StudentsViewAdapter studentsViewAdapter;    //for use with recyclerView


    private static final String TAG = "HomePageReceiver";   //for logging
    private MessageListener realListener;                   //nearby listener
    private FakedMessageListener fakedMessageListener = null;   // for mocking
                                                                // nearby
    private IBuilder builder = new StudentWithCoursesBuilder(); // for building
                                                                // SWCs

    // Student received from listener(s) (if mockedStudent != null,
    // receivedStudentWithCourses will refer to same object as mockedStudent
    private StudentWithCourses receivedStudentWithCourses = null;

    //Student made on return from the NMMActivity
    private StudentWithCourses mockedStudent = null;
    //String received from NMMActivity
    private String mockCSV = null;

    //used to access this activity in other classes
    private Context context;

    private String UUID;

    private String priority;

    //for getting the csv from NMMActivity
    ActivityResultLauncher<Intent> activityLauncher = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_page);
        setTitle("Birds of a Feather");

        //set this context
        this.context = this;

        //set priority to "common classes" as default
        priority = "common classes";

        //set thisStudent and thisStudentCourses
        db = AppDatabase.singleton(this);
        thisStudent = db.studentsDao().get(1);
        thisStudentCourses = db.coursesDao().getForStudent(1);

        //TODO test: OUTPUT current UUID for use with mocking
        UUID = thisStudent.getUUID();
        Log.d("UUID", UUID); //output UUID with tag UUID in console

        //create spinner (drop-down menu) for priorities/sorting algorithms
        p_spinner = findViewById(R.id.priority_spinner);
        ArrayAdapter<CharSequence> p_adapter = ArrayAdapter.createFromResource(this, R.array.priorities_array, android.R.layout.simple_spinner_item);
        p_adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        p_spinner.setAdapter(p_adapter);

        //listener for changing priority of list
        p_spinner.setOnItemSelectedListener(
                new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
                        Log.d(TAG, "Selecting priority...");
                        priority = parent.getItemAtPosition(pos).toString();
                        studentsViewAdapter.sortList(priority);
                        Log.d(TAG, "List sorted based on priority: "+priority);
                    }
                    @Override
                    public void onNothingSelected(AdapterView<?> parent) {
                    }
                });

        //set up RecyclerView
        studentsRecyclerView = findViewById(R.id.students_view);

        studentsLayoutManager = new LinearLayoutManager(this);
        studentsRecyclerView.setLayoutManager(studentsLayoutManager);

        studentsViewAdapter = new StudentsViewAdapter(new ArrayList<>());
        studentsRecyclerView.setAdapter(studentsViewAdapter);
        studentsViewAdapter.setContext(context);

        //set up listener for search button:
        toggleSearch = findViewById(R.id.search_button);
        toggleSearch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                onStartSearchingClicked();
            } else {
                onStopSearchingClicked();
            }
        });

        //create SWC builder
        builder = new StudentWithCoursesBuilder();

        //create activityLauncher for getting csv from NMM
        activityLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                new ActivityResultCallback<ActivityResult>() {
                    @Override
                    public void onActivityResult(ActivityResult result) {
                        if (result.getResultCode() != 0) {
                            Log.d(TAG, "result invalid, exit onActivityResult");
                            return;
                        }

                        Log.d(TAG, "Back from NMM");
                        Intent intent = result.getData();

                        if (intent == null) {
                            Log.d(TAG, "intent invalid, exit onActivityResult");
                            return;
                        }

                        Log.d(TAG, "Making mocked student from csv");
                        mockCSV = intent.getStringExtra("mockCSV");

                        if (mockCSV != null) {
                            mockedStudent = builder.setFromCSV(mockCSV)
                                    .getSWC();
                            Log.d(TAG, "Mocked student " + mockedStudent.getStudent().getName() + " created");

                        }
                        else { Log.d(TAG, "Mocked student is null, not created"); }
                    }
                });

        //create real listener to receive an SWC message, then updates lists as
        // needed
        realListener = new MessageListener() {
            @Override
            public void onFound(@NonNull Message message) {
                //make StudentWithCourses from byte array received
                Log.d(TAG, "found a (nonnull) message: " + new String(message.getContent()));
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
                Log.d(TAG, "message's UUID is: " + receivedStudentWithCourses.getStudent().getUUID());
                Log.d(TAG,
                        "message is a studentWithCourses named "
                                + receivedStudentWithCourses.getStudent().getName());
                Log.d(TAG, "message's waveTarget is: " + receivedStudentWithCourses.getStudent().getWaveTarget());

                //update the recyclerview list
                runOnUiThread(() -> {
                    updateLists();
                });
            }
            @Override
            public void onLost(@NonNull Message message) {
                Log.d(TAG, "Lost sight of message: " + new String(message.getContent()));
            }
        };

        Log.d(TAG, "realListener created");
    }

    @Override
    protected void onStart() {
        super.onStart();

        Log.d(TAG, "creating message to send through Nearby...");
        //create user's StudentWithCourses object to send to others via Bluetooth/Nearby API
        selfStudentWithCourses = new StudentWithCourses(thisStudent, thisStudentCourses);

        //make byte array for student and courses
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ObjectOutputStream out = null;
        byte[] studentWithCoursesBytes = new byte[0];
        try {
            out = new ObjectOutputStream(bos);
            out.writeObject(selfStudentWithCourses);
            out.flush();
            studentWithCoursesBytes = bos.toByteArray();
            bos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        byte[] finalStudentWithCoursesBytes = studentWithCoursesBytes;

        selfMessage = new Message(finalStudentWithCoursesBytes);
        Log.d(TAG, "MessagesClient.publish ("+Nearby.getMessagesClient(this).getClass().getSimpleName()+
                "): publishing selfMessage (StudentWithCourses)...");
        Nearby.getMessagesClient(this).publish(selfMessage);
        Log.d(TAG, "published selfMessage via Nearby API");
    }

    @Override
    protected void onDestroy() {
        Log.d(TAG, "MessagesClient.unpublish ("+Nearby.getMessagesClient(this).getClass().getSimpleName()+
                "): unpublishing selfMessage (StudentWithCourses)...");
        Nearby.getMessagesClient(this).unpublish(selfMessage);
        Log.d(TAG, "unpublished selfMessage");
        super.onDestroy();
    }

    @Override
    protected void onResume() {
        Log.d(TAG, "onResume called");
        super.onResume();
        if (mockedStudent != null && session != null) {
            Log.d(TAG, "onResume: updating fakedMessageListener with current mockedStudent " +
                    mockedStudent.getStudent().getName());
            this.fakedMessageListener = new FakedMessageListener(this.realListener, 3,
                    mockedStudent);
        }
    }

    // creates the fakedMessageListener if a mockedStudent exists and
    // subscribes the realListener for actual bluetooth. Once the fML is
    // made, it should immediately call realListener's onFound(), which calls
    // updateList()
    public void onStartSearchingClicked() {
        //clear students list every time Start is clicked
        if (!studentsViewAdapter.getStudents().isEmpty()) {
            studentsViewAdapter.clearStudents();
        }

        //get current date when Start Searching is clicked
        currDate = new Date();
        String currDateFormatted = sdf.format(currDate);
        Log.d(TAG, "Start clicked at time: " + currDateFormatted);
        createSession();

        //set up mock listener if a mockedStudent was made
        if (mockedStudent!=null) {
            Log.d(TAG, "MessagesClient.subscribe: mocked student found, " +
                    "subscribing fakedMessageListener...");

            this.fakedMessageListener = new FakedMessageListener(this.realListener, 3,
                    mockedStudent);
            Nearby.getMessagesClient(this).subscribe(fakedMessageListener);
        }

        //actual listener (not necessary for project)
        Log.d(TAG, "MessagesClient.subscribe: subscribing realListener...");
        Nearby.getMessagesClient(this).subscribe(realListener);
    }

    public void onStopSearchingClicked() {
        removeFakedML();

        //actual listener (not necessary for project)
        Log.d(TAG, "MessagesClient.unsubscribe: unsubscribing realListener...");
        Nearby.getMessagesClient(this).unsubscribe(realListener);

        //Stop clicked, create session
        Log.d(TAG, "Stop clicked");
        saveSession();
    }

    private void createSession() {
        String currDateFormatted = sdf.format(currDate);
        session = new Session("", currDateFormatted, currDateFormatted);
        Log.d(TAG, "created session at time: " + currDateFormatted);

        db.sessionsDao().insert(session);
        sessionId = db.sessionsDao().maxId();
    }

    private void saveSession() {
        //TODO: save/rename session with pop-up window (DialogFragment)
        //String sessionName = "";

        Toast.makeText(this, "Session saved", Toast.LENGTH_SHORT).show();
        //Log.d(TAG, "Saved Session with name: " + sessionName);

        session = null;
        sessionId = 0;
    }

    //removes fakedMessageListener if created
    public void removeFakedML() {
        Log.d(TAG, "removeFakedML called");
        if (fakedMessageListener != null) {
            Log.d(TAG, "MessagesClient.unsubscribe: " +
                    "unsubscribing and destroying fakedMessageListener...");

            Nearby.getMessagesClient(this).unsubscribe(fakedMessageListener);
            Log.d(TAG, "unsubscribed fakedMessageListener");
            fakedMessageListener = null;
        }
    }

    public void onGoToMockStudents(View view) {
        removeFakedML();

        Log.d(TAG, "going to NMM");
        Intent intent = new Intent(this, NearbyMessageMockActivity.class);

        activityLauncher.launch(intent);
    }

    public void onSessionsClicked(View view) {
        removeFakedML();

        Log.d(TAG, "going to Sessions");
        Intent intent = new Intent(this, SessionsActivity.class);
        startActivity(intent);
    }

    public void onAddClassesClicked(View view) {
        removeFakedML();

        Log.d(TAG, "going to InputCourses");
        Intent intent = new Intent(this, InputCourseActivity.class);
        //check if navigated from HomePageActivity or not (as opposed to PhotoActivity)
        intent.putExtra("onHomePage",true);
        startActivity(intent);
    }

    // called from listener, checks whether the student needs to be added to
    // homepage list and database
    private synchronized void updateLists()  {

        Student mStudent = receivedStudentWithCourses.getStudent();
        String mName = mStudent.getName();

        int matchingIndex = -1;

        //getMatchingStudent will throw NullPointerException if student doesn't exist
        try {
            matchingIndex = getMatchingStudent(mStudent);
            Log.d(TAG, "Discovered matching student: " + matchingIndex);
        }
        catch (NullPointerException e) {
            Log.d(TAG, "No matching student");
        }

        //exit early if student already on homepage
        if (studentsViewAdapter.getStudents().contains(mStudent)) {
            Log.d(TAG, "Student " + mName + " already in homepage list");
            if (matchingIndex != -1) {
                if (mStudent.getWaveTarget().equals(UUID)) {
                    //set existing student's waveAtMe to true on studentsViewAdapter
                    Log.d(TAG, "Discovered a matching wave");
                    studentsViewAdapter.getStudents().get(matchingIndex).setWavedAtMe(true);
                    //TODO test: update Adapter UI
                    //TODO test: update Adapter Sorting
                    studentsViewAdapter.sortList(priority);
                    //TODO update DB
                }
            }
            return;
        }

        Log.d(TAG, "student not in homepage list, checking common courses");

        //use BoFsTracker to find common classes
        ArrayList<Course> commonCourses = (ArrayList<Course>)
                BoFsTracker.getCommonCourses(
                        thisStudentCourses,
                        receivedStudentWithCourses.getCourses());

        //exit early if the student has no common courses
        if (commonCourses.size() == 0) {
            Log.d(TAG, "Student " + mName + " has no common courses");
            return;
        }

        Log.d(TAG,"studentWithCourses has a common class");

        //set the weights
        mStudent.setMatches(commonCourses.size());
        mStudent.setClassSizeWeight(BoFsTracker.calcClassSizeWeight(commonCourses));
        mStudent.setRecencyWeight(BoFsTracker.calcRecencyWeight(commonCourses));

        //overwrite common courses in rSWC to add to db later
        receivedStudentWithCourses.setCourses(commonCourses);

        //only add to db if not already in db
        if (!db.studentsDao().getAll().contains(mStudent)) {
            Log.d(TAG, "Student " + mName + " will be added to database");

            //insert the student and get its id in the db
            db.studentsDao().insert(mStudent);
            int insertedStuId = db.studentsDao().maxId();
            mStudent.setStudentId(insertedStuId);

            //get the id for courses to insert
            int currentMaxCourseId = db.coursesDao().maxId();
            for (Course receivedCourse : commonCourses) { //only common courses
                                                          //need to be added to db
                receivedCourse.setStudentId(insertedStuId);
                receivedCourse.setCourseId(++currentMaxCourseId);
                db.coursesDao().insert(receivedCourse);
            }
        }
        else {
            Log.d(TAG, "Student " + mName + " already in database");

            //set student id based on entry in database
            int dbId = db.studentsDao().getAll().indexOf(mStudent) + 1;
            mStudent.setStudentId(dbId);
        }

        //add to session, update database
        Log.d(TAG, "Preparing to add to student to session");
        String updatedList = (db.sessionsDao().get(sessionId).studentIDList)
                + "," + mStudent.getStudentId();
        db.sessionsDao().updateStudentList(sessionId, updatedList);

        //add this student to viewAdapter list
        Log.d(TAG, "preparing to add Student " + mName + " to recycler view");
        studentsViewAdapter.addStudent(receivedStudentWithCourses.getStudent());

        //resort the list
        Log.d(TAG, "student added, resorting the list...");
        studentsViewAdapter.sortList(p_spinner.getSelectedItem().toString());
        Log.d(TAG, "students list sorted");
    }

    //Helper function to find the existing student matching the new student
    private int getMatchingStudent(Student newStudent) {
        for(int i = 0; i < studentsViewAdapter.getStudents().size(); i++) {
            Log.d(TAG, "Index " + i + "'s UUID: " + studentsViewAdapter.getStudents().get(i).getUUID());
            if (studentsViewAdapter.getStudents().get(i).getUUID().equals(newStudent.getUUID())) {
                return i;
            }
        }
        return -1;
    }

    //for testing, need to be able to make mocked student without going to NMM
    public void setMockedStudent(StudentWithCourses stuWithCourses) {
        mockedStudent = stuWithCourses;
    }

    //for testing, need to switch working database with a test db
    public void setDb(AppDatabase db) {
        this.db = db;
    }

    //test method
    public StudentsViewAdapter getStudentsViewAdapter() {
        return studentsViewAdapter;
    }
}