package edu.ucsd.cse110.bof.viewProfile;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.Image;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.net.ssl.HttpsURLConnection;

import edu.ucsd.cse110.bof.InputCourses.CoursesViewAdapter;
import edu.ucsd.cse110.bof.R;
import edu.ucsd.cse110.bof.model.IStudent;
import edu.ucsd.cse110.bof.model.db.AppDatabase;
import edu.ucsd.cse110.bof.model.db.Course;
import edu.ucsd.cse110.bof.viewProfile.CoursesListViewAdapter;


public class StudentDetailActivity extends AppCompatActivity {

    private AppDatabase db;
    int studentID;
    private IStudent student;
    private List<Course> courses;
    private String studentImageURL;

    protected TextView studentName;
    protected ImageView studentImage;
    protected RecyclerView coursesRecyclerView;
    protected RecyclerView.LayoutManager coursesLayoutManager;
    protected CoursesListViewAdapter coursesListViewAdapter;

    private ExecutorService backgroundThreadExecutor =
            Executors.newSingleThreadExecutor();

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_profile);

        Intent intent = getIntent();
        studentID = intent.getIntExtra("student_id", 0); //get student id

        //get courses and student info from data base using studentID
        db = AppDatabase.singleton(this);


        //get student and their courses
        student = db.studentsDao().get(studentID);
        courses = db.coursesDao().getForStudent(studentID);


        //sets student name and picture
        studentName = findViewById(R.id.profile_name);
        studentName.setText(student.getName());

        //set image
        studentImage = findViewById(R.id.student_profile_img); //finds imageview

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


        //finds recycler for courses list
        coursesRecyclerView = findViewById(R.id.list_classes_recycler);

        //Sets RecyclerView layoutManager
        coursesLayoutManager = new LinearLayoutManager(this);
        coursesRecyclerView.setLayoutManager(coursesLayoutManager);

        //uses CoursesViewAdapter class to set courses into recycler
        coursesListViewAdapter = new CoursesListViewAdapter(courses);
        coursesRecyclerView.setAdapter(coursesListViewAdapter);

    }

    public void onHomeClicked(View view) {
        finish();
    }

    public static Bitmap getBitmapFromURL(String src) {
        try {
            URL url = new URL(src);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setDoInput(true);
            connection.connect();
            InputStream input = connection.getInputStream();
            Bitmap myBitmap = BitmapFactory.decodeStream(input);
            return myBitmap;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

}