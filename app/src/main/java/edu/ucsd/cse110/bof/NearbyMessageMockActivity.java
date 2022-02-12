package edu.ucsd.cse110.bof;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
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

public class NearbyMessageMockActivity extends AppCompatActivity {
    private static final String TAG = "MockingReceiver";
    private MessageListener messageListener;
    private MessageListener realListener;
    private EditText mockStudentInput;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_nearby_message_mock);

        // retrieve context
        Context context = this;

        mockStudentInput = findViewById(R.id.editName);

        //create the listener
        realListener = new MessageListener() {
            IStudent student = null;
            @Override
            public void onFound(@NonNull Message message) {
                //make IStudent from byte array received
                ByteArrayInputStream bis =
                        new ByteArrayInputStream(message.getContent());
                ObjectInput stuObj = null;
                try {
                    stuObj = new ObjectInputStream(bis);
                    student = (IStudent) stuObj.readObject();
                } catch (IOException | ClassNotFoundException e) {
                    e.printStackTrace();
                }

                //log the received student
                if (student != null) {
                    Log.d(TAG, "Received student: " + student.getName());
                    Log.d(TAG, "photoURL: " + student.getPhotoUrl());
                    Log.d(TAG, "Classes: ");
                    ArrayList<Course> courses =
                            (ArrayList<Course>) student.getCourses(context);
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
                Log.d(TAG, "Lost sight of: " + student.getName());
            }
        };
    }

    /**
     * Assumes that this input has valid csv, will create fake message
     * listener to mock a student being nearby (sends the student every 3
     * seconds)
     */
    public void onConfirmMockedStudent(View view) {
        IStudent student = makeMockedStudent();
        this.messageListener = new FakedMessageListener(realListener,
                3, student);

    }

    //should be moved into a separate class
    //makes a student from the CSV
    protected IStudent makeMockedStudent() {
        String csv = mockStudentInput.getText().toString();
        Scanner reader = new Scanner(csv).useDelimiter(",");

        Student stu = new Student();
        stu.setName(reader.next());
        reader.nextLine();

        stu.setPhotoUrl(reader.next());
        reader.nextLine();

        ArrayList<Course> courses = new ArrayList<>();

        int year;
        String quarter, subject, courseNum;

        while (reader.hasNext()) {
            year = reader.nextInt();
            quarter = reader.next();
            subject = reader.next();
            courseNum = reader.next();

            courses.add(new Course( 0, year,
                    quarter, subject, courseNum));
        }

        //TODO: fix logic
        //stu.courses = courses;

        return stu;
    }

    public void onGoBackClicked(View view) {
        if (messageListener != null) {
            Nearby.getMessagesClient(this).subscribe(messageListener);
        }

        Intent intent = new Intent(this, HomePageActivity.class);
        startActivity(intent);
    }
}