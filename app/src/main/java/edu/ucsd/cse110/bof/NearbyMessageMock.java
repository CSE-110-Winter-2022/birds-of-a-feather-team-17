package edu.ucsd.cse110.bof;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

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

import edu.ucsd.cse110.bof.model.IStudent;
import edu.ucsd.cse110.bof.model.db.Course;
import edu.ucsd.cse110.bof.model.db.StudentWithCourses;

public class NearbyMessageMock extends AppCompatActivity {
    private static final String TAG = "MockingReceiver";
    private MessageListener messageListener;
    private MessageListener realListener;
    private EditText mockStudentInput;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_nearby_message_mock);

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
                            (ArrayList<Course>) student.getCourses();
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

    //Assumes that the input has valid csv
    public void onConfirmMockedStudent(View view) {
        IStudent student = makeMockedStudent();
        this.messageListener = new FakedMessageListener(realListener,
                student);

    }

    @Override
    protected void onStart() {
        super.onStart();
        Nearby.getMessagesClient(this).subscribe(messageListener);
    }

    //should be moved into a separate class
    //makes a student from the CSV
    protected IStudent makeMockedStudent() {
        String csv = mockStudentInput.getText().toString();
        Scanner reader = new Scanner(csv).useDelimiter(",");

        StudentWithCourses stu = new StudentWithCourses();
        stu.student.name = reader.next();
        reader.nextLine();

        stu.student.photoURL = reader.next();
        reader.nextLine();

        ArrayList<Course> courses = new ArrayList<>();

        int year;
        String quarter, subject, courseNum;

        while (reader.hasNext()) {
            year = reader.nextInt();
            quarter = reader.next();
            subject = reader.next();
            courseNum = reader.next();

            courses.add(new Course(0, 0, year,
                    quarter, subject, courseNum));
        }

        stu.courses = courses;

        return stu;
    }
}