package edu.ucsd.cse110.bof.InputCourses;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_input_course);

        Spinner quarter_spinner = findViewById(R.id.fidget_spinner);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this, R.array.quarters_array, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        quarter_spinner.setAdapter(adapter);
        setTitle("Birds of a Feather");


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
        //make sure at least 1 course is entered TODO: test
        if (db.coursesDao().getForStudent(1).isEmpty()) {
            Toast.makeText(this, "Enter a course",Toast.LENGTH_SHORT).show();
            return;
        }

        //move to home page
        Intent intent = new Intent(this, HomePageActivity.class);
        intent.putExtra("student_id", 1);
        startActivity(intent);
    }

    public void onAddCourseClicked(View view) {
        //user's studentID is 1, first one inserted into database
        int studentID = 1;

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

        //null inputs
        if (newQuarterText.isEmpty() || newSubjectText.isEmpty() || newCourseNumText.isEmpty()) {
            Toast.makeText(this, "Enter valid course", Toast.LENGTH_SHORT).show();
            return;
        }

        //Make the course object and insert
        Course newCourse = new Course(studentID, newYearText, newQuarterText, newSubjectText, newCourseNumText);

        //check not duplicate course TODO: test
        List<Course> stuCoursesList = db.coursesDao().getForStudent(1);
        HashSet<Course> stuCourses = new HashSet<>(stuCoursesList);
        if (stuCourses.contains(newCourse)) {
            Toast.makeText(this, "Duplicate course", Toast.LENGTH_SHORT).show();
            return;
        }

        db.coursesDao().insert(newCourse);

        //update the courseViewAdapter to show this new course
        coursesViewAdapter.addCourse(newCourse);
    }
}