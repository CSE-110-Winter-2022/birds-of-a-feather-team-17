package edu.ucsd.cse110.bof.InputCourses;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;

import java.util.List;

import edu.ucsd.cse110.bof.InputCourses.CoursesViewAdapter;
import edu.ucsd.cse110.bof.R;
import edu.ucsd.cse110.bof.StudentWithCourses;
import edu.ucsd.cse110.bof.homepage.HomePageActivity;
import edu.ucsd.cse110.bof.login.PhotoActivity;
import edu.ucsd.cse110.bof.model.IStudent;
import edu.ucsd.cse110.bof.model.db.AppDatabase;
import edu.ucsd.cse110.bof.model.db.Course;
import edu.ucsd.cse110.bof.model.db.Student;

public class InputCourseActivity extends AppCompatActivity {
    private AppDatabase db;
    private StudentWithCourses studentWithCourses;

    protected RecyclerView coursesRecyclerView;
    protected RecyclerView.LayoutManager coursesLayoutManager;
    protected CoursesViewAdapter coursesViewAdapter;

    private static final String TAG = "InputCourseActivity";

    //user's studentID is 1, first one inserted into database
    private static final int USER_ID = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_input_course);

        Spinner quarter_spinner = findViewById(R.id.fidget_spinner);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this, R.array.quarters_array, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        quarter_spinner.setAdapter(adapter);
        setTitle("Birds of a Feather");

        //get student info from photo activity
        Intent intent = getIntent();

        String studentName = intent.getStringExtra("student_name");
        String studentPhoto = intent.getStringExtra("student_photo");

        db = AppDatabase.singleton(this);

        //Create new student
        /*
        student = db.studentsDao().get(studentID);
        student.studentId = studentID;
        student.name = studentName;
        student.photoURL = studentPhoto;
         */

        Log.d(TAG, "Received user's name: " + studentName);
        Log.d(TAG, "Received user's photoURL: " + studentPhoto);

        db.studentsDao().insert(new Student(studentName, studentPhoto));

        List<Course> courses = db.coursesDao().getForStudent(USER_ID);


        coursesRecyclerView = findViewById(R.id.courses_view);

        coursesLayoutManager = new LinearLayoutManager(this);
        coursesRecyclerView.setLayoutManager(coursesLayoutManager);

        coursesViewAdapter = new CoursesViewAdapter(courses, (course) -> {
            db.coursesDao().delete(course);
        });

        coursesRecyclerView.setAdapter(coursesViewAdapter);
    }

    public void onDoneClicked(View view) {
        //move to home page
        Intent intent = new Intent(this, HomePageActivity.class);
        intent.putExtra("student_id", USER_ID);
        startActivity(intent);
    }

    public void onAddCourseClicked(View view) {
        //find inputs
        Spinner newQuarterTextView = findViewById(R.id.fidget_spinner);
        TextView newYearTextView = findViewById(R.id.input_year);
        TextView newSubjectTextView = findViewById(R.id.input_subject);
        TextView newCourseNumTextView = findViewById(R.id.input_course_number);

        //get info from inputs
        String newQuarterText = newQuarterTextView.getSelectedItem().toString();
        int newYearText = Integer.parseInt(newYearTextView.getText().toString());
        String newSubjectText = newSubjectTextView.getText().toString();
        String newCourseNumText = newCourseNumTextView.getText().toString();

        //Make the course object and insert
        Course newCourse = new Course(USER_ID, newYearText, newQuarterText,
                newSubjectText, newCourseNumText);
        db.coursesDao().insert(newCourse);

        //update the courseViewAdapter to show this new course
        coursesViewAdapter.addCourse(newCourse);
    }
}