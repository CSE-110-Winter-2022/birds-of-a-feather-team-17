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
import android.widget.Toast;

import java.util.HashSet;
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

    protected RecyclerView coursesRecyclerView;
    protected RecyclerView.LayoutManager coursesLayoutManager;
    protected CoursesViewAdapter coursesViewAdapter;
    protected InputCourseHandler inputCourseHandler;

    private static final String TAG = "InputCourseActivity";

    //user's studentID is 1, first one inserted into database
    private static final int USER_ID = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_input_course);

        Spinner quarter_spinner = findViewById(R.id.fidget_spinner);
        ArrayAdapter<CharSequence> quarter_adapter = ArrayAdapter.createFromResource(this, R.array.quarters_array, android.R.layout.simple_spinner_item);
        quarter_adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        quarter_spinner.setAdapter(quarter_adapter);

        Spinner year_spinner = findViewById(R.id.input_year);
        ArrayAdapter<CharSequence> year_adapter = ArrayAdapter.createFromResource(this, R.array.years_array, android.R.layout.simple_spinner_item);
        year_adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        year_spinner.setAdapter(year_adapter);

        setTitle("Birds of a Feather");

        //create InputCourseHandler
        inputCourseHandler = new InputCourseHandler(this, false);

        //get student info from photo activity
        Intent intent = getIntent();
        String studentName = intent.getStringExtra("student_name");
        String studentPhoto = intent.getStringExtra("student_photo");

        //insert user into database (student_id=1, first element in database)
        db = AppDatabase.singleton(this);

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

        if (inputCourseHandler.getNumEntered() == 0) {
            Toast.makeText(this, "Enter a course",Toast.LENGTH_SHORT).show();
            return;
        }

        //move to home page
        Intent intent = new Intent(this, HomePageActivity.class);
        intent.putExtra("student_id", USER_ID);
        startActivity(intent);
        //finish();
    }

    public void onAddCourseClicked(View view) {

        //user's studentID is 1, first one inserted into database
        int studentID = 1;
        int courseID = db.coursesDao().maxId() + 1;

        //find inputs
        Spinner newQuarterTextView = findViewById(R.id.fidget_spinner);
        Spinner newYearTextView = findViewById(R.id.input_year);
        TextView newSubjectTextView = findViewById(R.id.input_subject);
        TextView newCourseNumTextView = findViewById(R.id.input_course_number);

        //get info from inputs
        String newQuarterText = newQuarterTextView.getSelectedItem().toString();
        int newYearText = Integer.parseInt(newYearTextView.getSelectedItem().toString());
        String newSubjectText = newSubjectTextView.getText().toString().toUpperCase();
        String newCourseNumText = newCourseNumTextView.getText().toString().toUpperCase();

        //have inputCourseHandler insert the course
        Course newCourse = inputCourseHandler.inputCourse(courseID,newYearText,
                newQuarterText, newSubjectText, newCourseNumText);

        //check for null and duplicate
        if (newCourse == null) {
            Toast.makeText(this, "Invalid class", Toast.LENGTH_SHORT).show();
        }
        else if (inputCourseHandler.getIsDuplicate()){
            Toast.makeText(this, "Course already entered", Toast.LENGTH_SHORT).show();
        }
        else {
            //update the courseViewAdapter to show this new course
            coursesViewAdapter.addCourse(newCourse);

        }
    }
}