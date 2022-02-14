package edu.ucsd.cse110.bof;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.nfc.Tag;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;

import com.google.android.gms.nearby.Nearby;
import com.google.android.gms.nearby.messages.Message;
import com.google.android.gms.nearby.messages.MessageListener;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.Scanner;

import edu.ucsd.cse110.bof.homepage.HomePageActivity;
import edu.ucsd.cse110.bof.model.IStudent;
import edu.ucsd.cse110.bof.model.db.Course;
import edu.ucsd.cse110.bof.model.db.Student;
import edu.ucsd.cse110.bof.StudentWithCourses;

public class NearbyMessageMockActivity extends AppCompatActivity {
    private static final String TAG = "MockingReceiver";
    private MessageListener fakedMessageListener;
    private MessageListener realListener;
    private EditText mockStudentInput;
    private ArrayList<Course> mockStuCourses;

    private StudentWithCourses studentWithCourses;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_nearby_message_mock);


        mockStudentInput = findViewById(R.id.editName);

        //create the listener (for confirming that it runs)
        realListener = new MessageListener() {
            StudentWithCourses studentWithCourses = null;
            @Override
            public void onFound(@NonNull Message message) {
                //make IStudent from byte array received
                ByteArrayInputStream bis =
                        new ByteArrayInputStream(message.getContent());
                ObjectInput stuObj = null;
                try {
                    stuObj = new ObjectInputStream(bis);
                    studentWithCourses = (StudentWithCourses) stuObj.readObject();
                } catch (IOException | ClassNotFoundException e) {
                    e.printStackTrace();
                }

                //log the received student
                if (studentWithCourses != null) {
                    Log.d(TAG,
                            "Received student: " + studentWithCourses.getStudent().getName());
                    Log.d(TAG,
                            "photoURL: " + studentWithCourses.getStudent().getPhotoUrl());
                    Log.d(TAG, "Classes: ");
                    ArrayList<Course> courses =
                            (ArrayList<Course>) studentWithCourses.getCourses();
                    for (Course course : courses) {
                        Log.d(TAG, course.toString());
                    }
                }
                else {
                    Log.d(TAG, "error");
                }
            }

            @Override
            public void onLost(@NonNull Message message) {
                Log.d(TAG, "Lost sight of: " + studentWithCourses.getStudent().getName());
            }
        };
    }

    /**
     * Assumes that this input has valid csv, will create fake message
     * listener to mock a student being nearby (sends the student every 3
     * seconds)
     */
    public void onConfirmMockedStudent(View view) {
        studentWithCourses = makeMockedStudent();
        this.fakedMessageListener = new FakedMessageListener(realListener,
                3, studentWithCourses);

        Nearby.getMessagesClient(this).subscribe(fakedMessageListener);

    }

    //should be moved into a separate class
    //makes a student from the CSV
    protected StudentWithCourses makeMockedStudent() {
        String csv = mockStudentInput.getText().toString();
        Scanner reader = new Scanner(csv).useDelimiter("[, \n]");

        Student mockStudent = new Student();
        mockStudent.setName(reader.next());
        reader.nextLine();

        mockStudent.setPhotoUrl(reader.next());
        reader.nextLine();

        mockStuCourses = new ArrayList<>();

        int year;
        String quarter, subject, courseNum;

        while (reader.hasNextLine()) {
            year = Integer.parseInt(reader.next());
            quarter = reader.next();
            subject = reader.next();
            courseNum = reader.next();
            reader.nextLine();

            mockStuCourses.add(new Course(1, 1, year,
                    quarter, subject, courseNum));
        }

        return new StudentWithCourses(mockStudent, mockStuCourses);
    }

    public void onGoBackClicked(View view) {
        Intent intent = new Intent(this, HomePageActivity.class);

        //send mocked student to homepage as serializable
        intent.putExtra("mockedStudent", this.studentWithCourses);
        startActivity(intent);
        finish();
    }
}