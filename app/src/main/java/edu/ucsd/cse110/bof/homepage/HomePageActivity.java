package edu.ucsd.cse110.bof.homepage;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.DialogFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
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
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import edu.ucsd.cse110.bof.BoFsTracker;
import edu.ucsd.cse110.bof.FakedMessageListener;
import edu.ucsd.cse110.bof.InputCourses.InputCourseActivity;
import edu.ucsd.cse110.bof.MockedStudentFactory;
import edu.ucsd.cse110.bof.NearbyMessageMockActivity;
import edu.ucsd.cse110.bof.R;
import edu.ucsd.cse110.bof.StudentWithCourses;
import edu.ucsd.cse110.bof.model.db.AppDatabase;
import edu.ucsd.cse110.bof.model.db.Course;
import edu.ucsd.cse110.bof.model.db.Session;
import edu.ucsd.cse110.bof.model.db.Student;
import edu.ucsd.cse110.bof.studentWithCoursesBytesFactory;
import edu.ucsd.cse110.bof.RenameDialogFragment;

public class HomePageActivity extends AppCompatActivity implements RenameDialogFragment.renameDialogListener {
    private AppDatabase db;
    private Student thisStudent;
    private List<Course> thisStudentCourses;
    private Message selfMessage;

    List<Student> myBoFs;
    Session session = null;
    int sessionId;

    ToggleButton toggleSearch;
    Spinner p_spinner;
    private Date currDate = null;
    @SuppressLint("ConstantLocale")
    private static final SimpleDateFormat sdf =
            new SimpleDateFormat("MM/dd/yy hh:mmaa", Locale.getDefault());

    RecyclerView studentsRecyclerView;
    RecyclerView.LayoutManager studentsLayoutManager;
    StudentsViewAdapter studentsViewAdapter;

    private static final String TAG = "HomePageReceiver";
    private MessageListener realListener;
    private FakedMessageListener fakedMessageListener = null;
    private MockedStudentFactory mockedStudentFactory;

    private StudentWithCourses selfStudentWithCourses;

    // Student received from listener(s) (if mockedStudent != null,
    // receivedStudentWithCourses will refer to same object as mockedStudent
    private StudentWithCourses receivedStudentWithCourses = null;

    //Student made on return from the NMMActivity
    private StudentWithCourses mockedStudent = null;
    private String mockCSV = null;

    private Context context;

    private String UUID;

    private String priority;

    /**
     * Retrieves mocked students entered in CSV format and turn them into mocked students
     */
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
                            if (mockedStudent == null) { Log.d(TAG, "Mocked student is null, not created"); }
                            else
                                Log.d(TAG, "Mocked student " + mockedStudent.getStudent().getName() + " created");
                        }
                    }
                }
    });

    /**
     * Instantiates components
     * @param savedInstanceState required for onCreate
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_page);
        setTitle("Birds of a Feather");

        context = this;

        // Initialize default priority to "common classes"
        priority = "common classes";

        // Initialize spinner (drop-down menu) for priorities/sorting algorithms
        p_spinner = findViewById(R.id.priority_spinner);
        ArrayAdapter<CharSequence> p_adapter = ArrayAdapter.createFromResource(this, R.array.priorities_array, android.R.layout.simple_spinner_item);
        p_adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        p_spinner.setAdapter(p_adapter);

        // Initialize the database with the user as its first entry
        db = AppDatabase.singleton(this);
        thisStudent = db.studentsDao().get(1);
        thisStudentCourses = db.coursesDao().getForStudent(1);

        // Initialize UUID for current student
        UUID = thisStudent.getUUID();
        Log.d("UUID", UUID); //output UUID with tag UUID in console

        // Initialize the RecyclerView layout for found BoFs
        myBoFs = new ArrayList<>();

        studentsRecyclerView = findViewById(R.id.students_view);

        studentsLayoutManager = new LinearLayoutManager(this);
        studentsRecyclerView.setLayoutManager(studentsLayoutManager);

        studentsViewAdapter = new StudentsViewAdapter(myBoFs);
        studentsRecyclerView.setAdapter(studentsViewAdapter);

        // Initialize listeners for drop-down menu selections
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

        // Initialize listeners for the start/stop search button
        toggleSearch = findViewById(R.id.search_button);
        toggleSearch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                onStartSearchingClicked();
            } else {
                onStopSearchingClicked();
            }
        });

        // Initialize factory for creating mocked students
        mockedStudentFactory = new MockedStudentFactory();

        // Initialize MessageListener for discovering nearby students
        realListener = new MessageListener() {
            @Override
            public void onFound(final Message message) {
                Toast.makeText(getApplicationContext(), "Found message!",Toast.LENGTH_SHORT).show();
                // Make StudentWithCourses from byte array received
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
                Log.d(TAG, "message's waveTarget is: " + receivedStudentWithCourses.getWaveTarget());

                // Update the recyclerview list
                updateLists();
            }
            @Override
            public void onLost(final Message message) {
                Log.d(TAG, "Lost sight of message: " + new String(message.getContent()));
            }
        };

        Log.d(TAG, "realListener created");

        //persistently store waveTargetUUID of current Message (starts with UUID="")
        SharedPreferences preferences = getPreferences(MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString("waveTargetUUID", "");
        editor.apply();

    }

    /**
     * Create and send out user's information on start
     */
    @Override
    protected void onStart() {
        super.onStart();

        // Create user's StudentWithCourses object to send to others via Bluetooth/Nearby API
        Log.d(TAG, "creating message to send through Nearby...");

        //retrieve current waveTargetUUID from SharedPreferences
        SharedPreferences preferences = getPreferences(MODE_PRIVATE);
        String currWaveTarget = preferences.getString("waveTargetUUID","");
        Log.d(TAG, "current wave target/ currWaveTarget is: " + currWaveTarget);

        selfStudentWithCourses = new StudentWithCourses(thisStudent, thisStudentCourses, currWaveTarget);
        byte[] finalStudentWithCoursesBytes = studentWithCoursesBytesFactory.convert(selfStudentWithCourses);
        selfMessage = new Message(finalStudentWithCoursesBytes);

        //Log the full message
        Log.d(TAG, "message has waveTarget: " + currWaveTarget);
        Log.d(TAG, "message has Student: " + thisStudent.getName());
        Log.d(TAG, "message has Student UUID: " + thisStudent.getUUID());

        // Send the message through the NearbyMessages API
        Log.d(TAG, "MessagesClient.publish ("+Nearby.getMessagesClient(this).getClass().getSimpleName()+
                "): publishing selfMessage (StudentWithCourses)...");
        Nearby.getMessagesClient(this).publish(selfMessage);
        Log.d(TAG, "published selfMessage via Nearby API");
    }

    @Override
    protected void onStop() {
        Log.d(TAG, "onStop called...");
        Log.d(TAG, "MessagesClient.unpublish ("+Nearby.getMessagesClient(this).getClass().getSimpleName()+
                "): unpublishing selfMessage (StudentWithCourses)...");
        Nearby.getMessagesClient(this).unpublish(selfMessage);
        Log.d(TAG, "unpublished selfMessage");
        super.onStop();
    }

    /**
     * Stop sending out the current user's info on exiting
     */
    @Override
    protected void onDestroy() {
        Log.d(TAG, "MessagesClient.unpublish ("+Nearby.getMessagesClient(this).getClass().getSimpleName()+
                "): unpublishing selfMessage (StudentWithCourses)...");
        Nearby.getMessagesClient(this).unpublish(selfMessage);
        Log.d(TAG, "unpublished selfMessage");
        super.onDestroy();
    }

    /**
     * Restore previous HomePageActivity state upon returning from a different activity
     */
    @Override
    protected void onResume() {
        // Restore activity state to previous upon returning
        Log.d(TAG, "onResume called");
        super.onResume();
        studentsViewAdapter.sortList(p_spinner.getSelectedItem().toString());
        if (mockedStudent != null && session != null) {
            Log.d(TAG, "onResume: updating fakedMessageListener with current mockedStudent " +
                    mockedStudent.getStudent().getName());
            this.fakedMessageListener = new FakedMessageListener(this.realListener, 3,
                    mockedStudent);
        }
    }

    /**
     * Creates the fakedMessageListener if a mockedStudent exists and subscribes the realListener
     * for actual bluetooth. Once the fML is made, it should immediately call realListener's
     * onFound(), which calls updateList().
     */
    public void onStartSearchingClicked() {
        // Clear students list every time Start is clicked
        if (!studentsViewAdapter.getStudents().isEmpty()) {
            studentsViewAdapter.clearStudents();
        }

        // Get current date when Start Searching is clicked
        currDate = new Date();
        String currDateFormatted = sdf.format(currDate);
        Log.d(TAG, "Start clicked at time: " + currDateFormatted);
        createSession();

        // Set up mock listener if a mockedStudent was made
        if (mockedStudent!=null) {
            Log.d(TAG, "MessagesClient.subscribe: mocked student found, " +
                    "subscribing fakedMessageListener...");

            this.fakedMessageListener = new FakedMessageListener(this.realListener, 3,
                    mockedStudent);
            Nearby.getMessagesClient(this).subscribe(fakedMessageListener);
        }

        // Actual listener for real BT function
        Log.d(TAG, "MessagesClient.subscribe: subscribing realListener...");
        Nearby.getMessagesClient(this).subscribe(realListener);
    }

    /**
     * Save the session and allow the user to rename it; disconnect the backend when search stops
     */
    public void onStopSearchingClicked() {
        removeFakedML();

        // Initialize dialog to allow for session renaming
        DialogFragment dialog = new RenameDialogFragment();
        dialog.show(getSupportFragmentManager(), "Rename dialog");

        // Actual listener for real BT functionality
        Log.d(TAG, "MessagesClient.unsubscribe: unsubscribing realListener...");
        Nearby.getMessagesClient(this).unsubscribe(realListener);

        // Stop clicked, create session
        Log.d(TAG, "Stop clicked");
    }

    /**
     * Create a session for retaining data in case the app is exited unexpectedly
     */
    private void createSession() {
        String currDateFormatted = sdf.format(currDate);
        session = new Session("", currDateFormatted, currDateFormatted);
        Log.d(TAG, "created session at time: " + currDateFormatted);

        db.sessionsDao().insert(session);
        sessionId = db.sessionsDao().maxId();
    }

    /**
     * Show the user the session is saved upon their click
     */
    private void saveSession() {
        Toast.makeText(this, "Session saved", Toast.LENGTH_SHORT).show();

        session = null;
        sessionId = 0;
    }

    /**
     * Removes fakeMessageListener (used for mocking)
     */
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

    /**
     * Jumps to NearbyMessageMockActivity on button click to add mocked students
     * @param view (required as it is an onClickListener)
     */
    public void onGoToMockStudents(View view) {
        removeFakedML();

        Log.d(TAG, "going to NMM");
        Intent intent = new Intent(this, NearbyMessageMockActivity.class);

        activityLauncher.launch(intent);
    }

    /**
     * Jumps to SessionsActivity on button click to view previous sessions and favorites
     * @param view (required as it is an onClickListener)
     */
    public void onSessionsClicked(View view) {
        removeFakedML();

        Log.d(TAG, "going to Sessions");
        Intent intent = new Intent(this, SessionsActivity.class);
        startActivity(intent);
    }

    /**
     * Jumps to InputCourseActivity on button click to add more classes
     * @param view (required as it is an onClickListener)
     */
    public void onAddClassesClicked(View view) {
        removeFakedML();

        Log.d(TAG, "going to InputCourses");
        Intent intent = new Intent(this, InputCourseActivity.class);
        //check if navigated from HomePageActivity or not (as opposed to PhotoActivity)
        intent.putExtra("onHomePage",true);
        startActivity(intent);
    }

    /**
     * Simple helper function to calculate weight of class sizes
     * @param courses with information on course size
     * @return weight of course given class size
     */
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

    /**
     * Simple helper function to calculate weight of how recently the class was taken
     * @param courses with information on course recency
     * @return weight of course given recency
     */
    public static int calcRecencyWeight(ArrayList<Course> courses) {
        if(courses == null) { return 0; }
        int sum = 0;
        for(Course c : courses) {
            if (c.year == 2022 && c.quarter.equals("WI")) { sum+=5; continue;}
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

    /**
     * Update list upon receiving new student info (new student or a wave)
     */
    private void updateLists()  {
        Student newStudent = receivedStudentWithCourses.getStudent();
        Log.d(TAG, "Received student id (before): " + newStudent.getStudentId());
        String newName = newStudent.getName();
        newStudent.setStudentId(0);
        Log.d(TAG, "Received student id (after): " + newStudent.getStudentId());
        int matchingIndex = -1; // Indicates that no student in existing list matches newStudent

        // GetMatchingStudent will throw NullPointerException if student doesn't exist
        try {
            matchingIndex = getMatchingStudent(newStudent);
            Log.d(TAG, "Discovered matching student's index: " + matchingIndex);
        }
        catch (NullPointerException e) {
            Log.d(TAG, "No matching student");
        }

        // Check that this student isn't in list nor in database
        if (studentsViewAdapter.getStudents().contains(newStudent)) {
            Log.d(TAG, "Student " + newName + " already in homepage list");
            if (matchingIndex != -1) {
                // If received message is actually a wave from an existing student
                if (receivedStudentWithCourses.getWaveTarget().equals(UUID)) {
                    Log.d(TAG, "Discovered a matching wave");

                    Student matchingStudent = studentsViewAdapter.getStudents().get(matchingIndex);
                    boolean wavedAlready = db.studentsDao().get(matchingStudent.getStudentId()).isWavedTo();

                    matchingStudent.setWavedAtMe(true);
                    matchingStudent.setWavedTo(wavedAlready);

                    db.studentsDao().updateWaveMe(matchingStudent.getStudentId(), true);

                    studentsViewAdapter.sortList(priority);
                }
            }
            return;
        }
        else {
            Log.d(TAG, "student not in homepage list");

            // Use BoFsTracker to find common classes
            ArrayList<Course> commonCourses = (ArrayList<Course>)
                    BoFsTracker.getCommonCourses(
                            thisStudentCourses,
                            receivedStudentWithCourses.getCourses());

            // If at least 1 common course, add this student to list of students and the database
            if (commonCourses.size() == 0) {
                Log.d(TAG, "Student " + newName + " has no common courses");
                return;
            }
            else {
                Log.d(TAG,"studentWithCourses has a common class");

                //add this student to viewAdapter list
                newStudent.setMatches(commonCourses.size());

                newStudent.setClassSizeWeight(calcClassSizeWeight(commonCourses));

                newStudent.setRecencyWeight(calcRecencyWeight(commonCourses));

                receivedStudentWithCourses.setCourses(commonCourses);

                //only add to db if not already in db
                if (!db.studentsDao().getAll().contains(newStudent)) {
                    Log.d(TAG, "Student " + newName + " will be added to database");
                    db.studentsDao().insert(newStudent);

                    int insertedId = db.studentsDao().maxId();
                    newStudent.setStudentId(insertedId);

                    //add to session, update database
                    String updatedList = (db.sessionsDao().get(sessionId).studentIDList) + "," + insertedId;
                    db.sessionsDao().updateStudentList(sessionId, updatedList);

                    int insertedCourseId = db.coursesDao().maxId();

                    //only common courses need to be added to db
                    for (Course receivedCourse : commonCourses) {
                        receivedCourse.setStudentId(insertedId);
                        receivedCourse.setCourseId(++insertedCourseId);
                        db.coursesDao().insert(receivedCourse);
                    }
                }
                else {
                    Log.d(TAG, "Student " + newName + " already in database");

                    //set student id based on entry in database
                    int dbId = db.studentsDao().getAll().indexOf(newStudent) + 1;
                    newStudent.setStudentId(dbId);

                    //add to session, update database
                    String updatedList = (db.sessionsDao().get(sessionId).studentIDList) + "," + dbId;
                    db.sessionsDao().updateStudentList(sessionId, updatedList);
                }

                Log.d(TAG, "preparing to add Student " + newName + " to recycler view");

                studentsViewAdapter.setContext(context);
                studentsViewAdapter.addStudent(receivedStudentWithCourses.getStudent());
                //studentsViewAdapter.setContext(null);

                //resort the list
                Log.d(TAG, "student added, resorting the list...");
                studentsViewAdapter.sortList(p_spinner.getSelectedItem().toString());
                Log.d(TAG, "students list sorted");
            }
        }
    }

    /**
     * Helper function to find the existing student in the list matching the new student
     * @param newStudent the new student we receive through Nearby
     * @return int the index of the existing student on the list, -1 if not in list
     */
    private int getMatchingStudent(Student newStudent) {
        for(int i = 0; i < studentsViewAdapter.getStudents().size(); i++) {
            Log.d(TAG, "Index " + i + "'s UUID: " + studentsViewAdapter.getStudents().get(i).getUUID());
            if (studentsViewAdapter.getStudents().get(i).getUUID().equals(newStudent.getUUID())) {
                return i;
            }
        }
        return -1;
    }

    /**
     * Simple setter for mocked student
     * @param stuWithCourses
     */
    public void setMockedStudent(StudentWithCourses stuWithCourses) {
        mockedStudent = stuWithCourses;
    }

    /**
     * Simple setter for db
     * @param db
     */
    public void setDb(AppDatabase db) {
        this.db = db;
    }

    /**
     * Simple getter for studentsViewAdapter
     * @return StudentsViewAdapter
     */
    public StudentsViewAdapter getStudentsViewAdapter() {
        return studentsViewAdapter;
    }

    /**
     * Saves the session when the session name is confirmed by the user
     * @param dialog
     */
    @Override
    public void onDialogConfirmed(RenameDialogFragment dialog) {
        EditText name = dialog.getView().findViewById(R.id.dialog_session_name);
        db.sessionsDao().updateDispName(sessionId, name.getText().toString());
        saveSession();
        Log.d(TAG, "Dialog confirmed");
    }

    /**
     * Saves the session with a default name is session name dialog is exited
     */
    @Override
    public void onDialogCanceled() {
        saveSession();
        Log.d(TAG, "Dialog canceled");
    }
}