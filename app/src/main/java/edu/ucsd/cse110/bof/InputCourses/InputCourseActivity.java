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

import java.util.List;

import edu.ucsd.cse110.bof.R;
import edu.ucsd.cse110.bof.homepage.HomePageActivity;
import edu.ucsd.cse110.bof.model.db.AppDatabase;
import edu.ucsd.cse110.bof.model.db.Course;

public class InputCourseActivity extends AppCompatActivity {
    private AppDatabase db;

    private boolean onHomePage;

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

        Spinner quarter_spinner = findViewById(R.id.input_qtr);
        ArrayAdapter<CharSequence> quarter_adapter = ArrayAdapter.createFromResource(this, R.array.quarters_array, android.R.layout.simple_spinner_item);
        quarter_adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        quarter_spinner.setAdapter(quarter_adapter);

        Spinner year_spinner = findViewById(R.id.input_year);
        ArrayAdapter<CharSequence> year_adapter = ArrayAdapter.createFromResource(this, R.array.years_array, android.R.layout.simple_spinner_item);
        year_adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        year_spinner.setAdapter(year_adapter);

        Spinner size_spinner = findViewById(R.id.input_size);
        ArrayAdapter<CharSequence> size_adapter = ArrayAdapter.createFromResource(this, R.array.sizes_array, android.R.layout.simple_spinner_item);
        size_adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        size_spinner.setAdapter(size_adapter);

        setTitle("Enter class history");

        //Retrieve onHomePage (boolean) sent from either PhotoActivity (false) or HomePageActivity (true)
        Bundle extras = getIntent().getExtras();
        onHomePage = extras.getBoolean("onHomePage");

        //create InputCourseHandler
        inputCourseHandler = new InputCourseHandler(this);

        //insert user into database (student_id=1, first element in database)
        db = AppDatabase.singleton(this);

        //fetch courses list from user (student_id=1 in database)
        List<Course> courses = db.coursesDao().getForStudent(1);

        coursesRecyclerView = findViewById(R.id.courses_view);

        coursesLayoutManager = new LinearLayoutManager(this);
        coursesRecyclerView.setLayoutManager(coursesLayoutManager);

        coursesViewAdapter = new CoursesViewAdapter(courses, (course) -> {
            db.coursesDao().delete(course);
        });

        coursesRecyclerView.setAdapter(coursesViewAdapter);
    }

    public void onDoneClicked(View view) {
        AppDatabase db = AppDatabase.singleton(this);
        if (db.coursesDao().getForStudent(USER_ID).isEmpty()) {
            Toast.makeText(this, "Enter a course",Toast.LENGTH_SHORT).show();
            return;
        }

        //move to home page
        if (onHomePage) {
            Log.d(TAG, "Arrived from homepage, returning to HomePageActivity");
            finish();
        }
        else {
            Intent intent = new Intent(this, HomePageActivity.class);
            intent.putExtra("student_id", USER_ID);
            startActivity(intent);
            finish();
        }
    }

    public void onAddCourseClicked(View view) {
        int courseID = db.coursesDao().maxId() + 1;

        //find inputs
        Spinner newQuarterTextView = findViewById(R.id.input_qtr);
        Spinner newYearTextView = findViewById(R.id.input_year);
        TextView newSubjectTextView = findViewById(R.id.input_subject);
        TextView newCourseNumTextView = findViewById(R.id.input_course_number);
        Spinner newSizeTextView = findViewById(R.id.input_size);

        //get info from inputs
        String newQuarterText = newQuarterTextView.getSelectedItem().toString();
        int newYearText = Integer.parseInt(newYearTextView.getSelectedItem().toString());
        String newSubjectText = newSubjectTextView.getText().toString().toUpperCase();
        String newCourseNumText = newCourseNumTextView.getText().toString().toUpperCase();
        String newSizeText = newSizeTextView.getSelectedItem().toString();

        //have inputCourseHandler insert the course
        Course newCourse = inputCourseHandler.inputCourse(courseID,newYearText,
                newQuarterText, newSubjectText, newCourseNumText, newSizeText);

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
            Log.d("Database size after adding new course", Integer.toString(db.coursesDao().count()));
        }
    }
}