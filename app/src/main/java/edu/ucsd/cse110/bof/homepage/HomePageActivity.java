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
import edu.ucsd.cse110.bof.InputCourses.InputCourseActivity;
import edu.ucsd.cse110.bof.MockedStudentFactory;
import edu.ucsd.cse110.bof.NearbyMessageMockActivity;
import edu.ucsd.cse110.bof.R;
import edu.ucsd.cse110.bof.StudentWithCourses;
import edu.ucsd.cse110.bof.model.db.AppDatabase;
import edu.ucsd.cse110.bof.model.db.Course;
import edu.ucsd.cse110.bof.model.db.Session;
import edu.ucsd.cse110.bof.model.db.Student;
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
                            mockCSV = intent.getStringExtra("mockCSV");
                            mockedStudent = mockedStudentFactory.makeMockedStudent(mockCSV);
                            if (mockedStudent == null) { Log.d(TAG, "Mocked student is null, not created"); }
                            else
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

        //set thisStudent and thisStudentCourses
        db = AppDatabase.singleton(this);
        thisStudent = db.studentsDao().get(1);
        thisStudentCourses = db.coursesDao().getForStudent(1);

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
                Log.d(TAG,
                        "message is a studentWithCourses named "
                                + receivedStudentWithCourses.getStudent().getName());

                //update the recyclerview list
                updateLists();
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

        DialogFragment dialog = new RenameDialogFragment();

        dialog.show(getSupportFragmentManager(), "Rename dialog");

        //actual listener (not necessary for project)
        Log.d(TAG, "MessagesClient.unsubscribe: unsubscribing realListener...");
        Nearby.getMessagesClient(this).unsubscribe(realListener);

        //Stop clicked, create session
        Log.d(TAG, "Stop clicked");
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
    private void updateLists()  {

        Student newStudent = receivedStudentWithCourses.getStudent();
        String newName = newStudent.getName();

        //check that this student isn't in list nor in database
        if (studentsViewAdapter.getStudents().contains(newStudent)) {
            Log.d(TAG, "Student " + newName + " already in homepage list");
            return;
        }
        else {
            Log.d(TAG, "student not in homepage list");

            //use BoFsTracker to find common classes
            ArrayList<Course> commonCourses = (ArrayList<Course>)
                    BoFsTracker.getCommonCourses(
                            thisStudentCourses,
                            receivedStudentWithCourses.getCourses());

            //if at least 1 common course, add this student to list of students
            //and the database
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

    @Override
    public void onDialogConfirmed(RenameDialogFragment dialog) {
        EditText name = dialog.getView().findViewById(R.id.dialog_session_name);
        db.sessionsDao().updateDispName(sessionId, name.getText().toString());
        saveSession();
        Log.d(TAG, "Dialog confirmed");
    }

    @Override
    public void onDialogCanceled() {
        saveSession();
        Log.d(TAG, "Dialog canceled");
    }
}