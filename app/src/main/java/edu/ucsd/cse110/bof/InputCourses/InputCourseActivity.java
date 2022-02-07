package edu.ucsd.cse110.bof.InputCourses;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import java.util.List;

import edu.ucsd.cse110.bof.InputCourses.CoursesViewAdapter;
import edu.ucsd.cse110.bof.R;
import edu.ucsd.cse110.bof.model.IStudent;
import edu.ucsd.cse110.bof.model.db.AppDatabase;
import edu.ucsd.cse110.bof.model.db.Course;

public class InputCourseActivity extends AppCompatActivity {
    private AppDatabase db;
    private IStudent student;

    protected RecyclerView coursesRecyclerView;
    protected RecyclerView.LayoutManager coursesLayoutManager;
    protected CoursesViewAdapter coursesViewAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_input_course);
        setTitle("Birds of a Feather");

        Intent intent = getIntent();
        int studentID = intent.getIntExtra("student_id", 0);

        db = AppDatabase.singleton(this);
        student = db.studentWithCoursesDao().get(studentID);
        List<Course> courses = db.coursesDao().getForStudent(studentID);

        coursesRecyclerView = findViewById(R.id.courses_view);

        coursesLayoutManager = new LinearLayoutManager(this);
        coursesRecyclerView.setLayoutManager(coursesLayoutManager);

        coursesViewAdapter = new CoursesViewAdapter(courses);
        coursesRecyclerView.setAdapter(coursesViewAdapter);
    }

    public void onDoneClicked(View view) {
        finish();
    }

    public void onAddCourseClicked(View view) {
        int newCourseID = db.coursesDao().count() + 1;
        int studentID = student.getId();
//        int studentID = 0;
        TextView newQuarterTextView = findViewById(R.id.input_quarter);
        TextView newYearTextView = findViewById(R.id.input_year);
        TextView newSubjectTextView = findViewById(R.id.input_subject);
        TextView newCourseNumTextView = findViewById(R.id.input_course_number);
        String newQuarterText = newQuarterTextView.getText().toString();
        int newYearText = Integer.parseInt(newYearTextView.getText().toString());
        String newSubjectText = newSubjectTextView.getText().toString();
        String newCourseNumText = newCourseNumTextView.getText().toString();

        Course newCourse = new Course(newCourseID, studentID, newYearText, newQuarterText, newSubjectText, newCourseNumText);
        db.coursesDao().insert(newCourse);

        coursesViewAdapter.addCourse(newCourse);
    }
}