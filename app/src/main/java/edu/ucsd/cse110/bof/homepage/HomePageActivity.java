package edu.ucsd.cse110.bof.homepage;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.TextView;
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

import edu.ucsd.cse110.bof.BoFsTracker;
import edu.ucsd.cse110.bof.FakedMessageListener;
import edu.ucsd.cse110.bof.InputCourses.CoursesViewAdapter;
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

    List<IStudent> myBoFs;

    RecyclerView studentsRecyclerView;
    RecyclerView.LayoutManager studentsLayoutManager;
    StudentsViewAdapter studentsViewAdapter;

    private static final String TAG = "HomePageReceiver";
    private MessageListener realListener;
    private MessageListener fakedMessageListener;

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

        //set up listener for search button:
        ToggleButton toggle = findViewById(R.id.search_button);
        toggle.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                onStartSearchingClicked();
            } else {
                onStopSearchingClicked();
            }
        });

        //set up actual listener for finding BoFs
        realListener = new MessageListener() {
            StudentWithCourses receivedStudentWithCourses = null;
            @Override
            public void onFound(@NonNull Message message) {
                //make StudentWithCourses from byte array received

                Log.d(TAG, "found a message");
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

                if (!myBoFs.contains(receivedStudentWithCourses.getStudent())) {
                    //use BoFsTracker to find common classes

                    Log.d(TAG,
                            "message is a studentWithCourses named "
                                    + receivedStudentWithCourses.getStudent().getName());
                    ArrayList<Course> commonCourses = (ArrayList<Course>)
                            BoFsTracker.getCommonCourses(
                                    thisStudent.getCourses(getApplicationContext()),
                                    receivedStudentWithCourses.getCourses());

                    //if not empty list, add this student to list of students
                    //and the database
                    if (commonCourses.size() != 0) {
                        Log.d(TAG,"studentWithCourses has a common class");
                        myBoFs.add(receivedStudentWithCourses.getStudent());

                        receivedStudentWithCourses.getStudent().setMatches(commonCourses.size());
                        db.studentsDao().insert((Student) receivedStudentWithCourses.getStudent());

                        for (Course receivedCourse :
                                receivedStudentWithCourses.getCourses()) {
                            db.coursesDao().insert(receivedCourse);
                        }

                        studentsViewAdapter.itemInserted();
                    }
                }
            }
        };

        //set up mock listener for receiving mocked items
        this.fakedMessageListener = new FakedMessageListener(this.realListener, 3,
                        (StudentWithCourses) intent.getSerializableExtra("mockedStudent") );
    }

    /**
     * Creates the listener to start searching for BoFs,
     */
    public void onStartSearchingClicked() {
        Nearby.getMessagesClient(this).subscribe(realListener);
        Nearby.getMessagesClient(this).subscribe(fakedMessageListener);
    }

    public void onStopSearchingClicked() {
        if (realListener != null) {
            Nearby.getMessagesClient(this).unsubscribe(realListener);
            Nearby.getMessagesClient(this).unsubscribe(fakedMessageListener);

        }
    }

    public void onGoToMockStudents(View view) {
        Intent intent = new Intent(this, NearbyMessageMockActivity.class);
        startActivity(intent);
    }

    public void onHistoryClicked(View view) {
        Intent intent = new Intent(this, HistoryActivity.class);
        startActivity(intent);
    }
}