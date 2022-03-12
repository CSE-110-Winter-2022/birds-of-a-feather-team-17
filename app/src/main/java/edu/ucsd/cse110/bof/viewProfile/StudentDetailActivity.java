package edu.ucsd.cse110.bof.viewProfile;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.nearby.Nearby;
import com.google.android.gms.nearby.messages.Message;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.net.ssl.HttpsURLConnection;

import edu.ucsd.cse110.bof.R;
import edu.ucsd.cse110.bof.model.StudentWithCourses;
import edu.ucsd.cse110.bof.model.db.AppDatabase;
import edu.ucsd.cse110.bof.model.db.Course;
import edu.ucsd.cse110.bof.model.db.Student;
import edu.ucsd.cse110.bof.studentWithCoursesBytesFactory;

/**
 * Activity for viewing a student's detailed profile
 */
public class StudentDetailActivity extends AppCompatActivity {

    private static final String TAG = "StudentDetailActivityLog: ";
    private AppDatabase db;
    int studentID;
    private Student student;
    private Student userStudent;
    private List<Course> courses;
    private List<Course> userCourses;
    private String studentImageURL;
    private boolean waveOn = false;

    protected TextView studentName;
    protected ImageView studentImage;
    protected RecyclerView coursesRecyclerView;
    protected ImageButton waveButton;
    protected RecyclerView.LayoutManager coursesLayoutManager;
    protected CoursesListViewAdapter coursesListViewAdapter;

    private ExecutorService backgroundThreadExecutor =
            Executors.newSingleThreadExecutor();

    /**
     * Initialize UI and backened components
     * @param savedInstanceState required for onCreate
     */
    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_profile);

        waveButton = findViewById(R.id.wave_icon);

        Intent intent = getIntent();
        studentID = intent.getIntExtra("student_id", 0); //get student id

        //get courses and student info from data base using studentID
        db = AppDatabase.singleton(this);

        //get student and their courses
        student = db.studentsDao().get(studentID);
        courses = db.coursesDao().getForStudent(studentID);
        userStudent = db.studentsDao().get(1);
        userCourses = db.coursesDao().getForStudent(1);

        // Sets student name
        studentName = findViewById(R.id.profile_name);
        studentName.setText(student.getName());

        studentImage = findViewById(R.id.student_profile_img);

        // Sets student photo
        backgroundThreadExecutor.submit(() -> {
            URL photo_url = null;
            try {
                photo_url = new URL(student.getPhotoUrl());
            } catch (MalformedURLException e) {
                e.printStackTrace();
            }
            Bitmap photoBitmap = null;
            try {
                HttpsURLConnection connection =
                        (HttpsURLConnection) Objects.requireNonNull(photo_url).openConnection();
                connection.setDoInput(true);
                photoBitmap = BitmapFactory.decodeStream(connection.getInputStream());
            } catch (IOException e) {
                e.printStackTrace();
            }

            final Bitmap myBitmap = photoBitmap;
            runOnUiThread(() -> {
                studentImage.setImageBitmap(myBitmap);
            });
        });

        // Finds recycler for courses list
        coursesRecyclerView = findViewById(R.id.list_classes_recycler);

        // Sets RecyclerView layoutManager
        coursesLayoutManager = new LinearLayoutManager(this);
        coursesRecyclerView.setLayoutManager(coursesLayoutManager);

        // Uses CoursesViewAdapter class to set courses into recycler
        coursesListViewAdapter = new CoursesListViewAdapter(courses);
        coursesRecyclerView.setAdapter(coursesListViewAdapter);

        // Set wave to be pre-filled if already waved
        if(student.isWavedTo())
        {
            waveOn = true;
            //Change image icon and accompanying description
            waveButton.setImageResource(R.drawable.wave_filled);
            waveButton.setContentDescription(getApplicationContext().getString(R.string.wave_on));
        }
    }

    /**
     * Go back to previous activity on back button pressed
     * @param view required for onClickListener
     */
    public void onBackClicked(View view) {
        finish();
    }

    /**
     * Show a toast, light the icon, and send a wave message when hollow hand is pressed
     * @param view required for onClickListener
     */
    public void onWaveClicked(View view) {
        // Do not send waves more than once
        if(!waveOn) {
            waveOn = true;

            // Change image icon and accompanying description
            waveButton.setImageResource(R.drawable.wave_filled);
            waveButton.setContentDescription(getApplicationContext().getString(R.string.wave_on));

            db.studentsDao().updateWaveTo(student.getStudentId(), true);

            Log.d(TAG, "waveTarget has UUID: " + student.getUUID());

            //edit waveTargetUUID in SharedPreferences to contain new wave target
            SharedPreferences preferences = getSharedPreferences("DEFAULT",MODE_PRIVATE);
            SharedPreferences.Editor editor = preferences.edit();
            editor.putString("waveTargetUUID", student.getUUID());
            editor.apply();

            // Display a toast declaring wave was sent
            Toast.makeText(this, "Wave sent!", Toast.LENGTH_SHORT).show();
        }
    }

    // Testing, need to set Db after onCreate to have views populate with test student
    public void setDb(AppDatabase db) {
        this.db = db;
    }

    // Testing, need to set id after onCreate to have views populate with test student
    public void setStudentID(int studentID) {
        this.studentID = studentID;
    }
}