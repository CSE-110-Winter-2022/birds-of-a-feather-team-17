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
import edu.ucsd.cse110.bof.InputCourses.CoursesViewAdapter;
import edu.ucsd.cse110.bof.NearbyMessageMockActivity;
import edu.ucsd.cse110.bof.R;
import edu.ucsd.cse110.bof.model.IStudent;
import edu.ucsd.cse110.bof.model.db.AppDatabase;
import edu.ucsd.cse110.bof.model.db.Course;
import edu.ucsd.cse110.bof.model.db.Student;

public class HomePageActivity extends AppCompatActivity {
    private AppDatabase db;
    private IStudent thisStudent;
    private Context context;

    List<IStudent> studentsFound;

    RecyclerView studentsRecyclerView;
    RecyclerView.LayoutManager studentsLayoutManager;
    StudentsViewAdapter studentsViewAdapter;

    private static final String TAG = "HomePageReceiver";
    private MessageListener realListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_page);
        setTitle("Birds of a Feather");

        context = this;

        //set thisStudent
        Intent intent = getIntent();
        int studentID = intent.getIntExtra("student_id", 0);
        db = AppDatabase.singleton(context);
        thisStudent = db.studentsDao().get(studentID);


        //set up RecyclerView
        studentsFound = new ArrayList<>();

        studentsRecyclerView = findViewById(R.id.students_view);

        studentsLayoutManager = new LinearLayoutManager(this);
        studentsRecyclerView.setLayoutManager(studentsLayoutManager);

        studentsViewAdapter = new StudentsViewAdapter(studentsFound);
        studentsRecyclerView.setAdapter(studentsViewAdapter);

        //set up toggle listener:
        ToggleButton toggle = findViewById(R.id.search_button);
        toggle.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    onStartSearchingClicked();
                } else {
                    onStopSearchingClicked();
                }
            }
        });
    }

    /**
     * Creates the listener to start searching for BoFs,
     */
    public void onStartSearchingClicked() {
        //set up listener for finding BoFs
        realListener = new MessageListener() {
            IStudent receivedStudent = null;
            @Override
            public void onFound(@NonNull Message message) {
                //make IStudent from byte array received
                ByteArrayInputStream bis =
                        new ByteArrayInputStream(message.getContent());
                ObjectInput stuObj = null;
                try {
                    stuObj = new ObjectInputStream(bis);
                    receivedStudent = (IStudent) stuObj.readObject();
                } catch (IOException | ClassNotFoundException e) {
                    e.printStackTrace();
                }

                if (!studentsFound.contains(receivedStudent)) {
                    //use BoFsTracker to find common classes
                    ArrayList<Course> commonCourses = (ArrayList<Course>)
                            BoFsTracker.getCommonCourses(context, thisStudent,
                                    receivedStudent);

                    //if not empty list, add this student to list of students
                    if (commonCourses.size() != 0) {
                        studentsFound.add(receivedStudent);

                        //TODO: sort students by num of commonCourses
                    }
                }
            }
        };

        Nearby.getMessagesClient(this).subscribe(realListener);
    }

    public void onStopSearchingClicked() {
        if (realListener != null) {
            Nearby.getMessagesClient(this).unsubscribe(realListener);
        }
    }

    public void onGoToMockStudents(View view) {
        Intent intent = new Intent(this, NearbyMessageMockActivity.class);
        startActivity(intent);
    }

}