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

import java.util.List;

import edu.ucsd.cse110.bof.InputCourses.CoursesViewAdapter;
import edu.ucsd.cse110.bof.R;
import edu.ucsd.cse110.bof.homepage.HomePageActivity;
import edu.ucsd.cse110.bof.login.PhotoActivity;
import edu.ucsd.cse110.bof.model.IStudent;
import edu.ucsd.cse110.bof.model.db.AppDatabase;
import edu.ucsd.cse110.bof.model.db.Course;
import edu.ucsd.cse110.bof.model.db.Student;
import edu.ucsd.cse110.bof.model.db.StudentWithCourses;

public class InputCourseActivity extends AppCompatActivity {
    private AppDatabase db;
    private Student student;
    private StudentWithCourses studentWithCourses;

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

        //get student info from photo activity
        Intent intent = getIntent();
        int studentID = intent.getIntExtra("student_id", 0);
        String studentName = intent.getStringExtra("student_name");
        String studentPhoto = intent.getStringExtra("student_photo");

        db = AppDatabase.singleton(this);

        //Create new student
//        student = db.studentWithCoursesDao().get(studentID);
//        student.studentId = studentID;
//        student.name = studentName;
//        student.photoURL = studentPhoto;

        List<Course> courses = db.coursesDao().getForStudent(studentID);

//        studentWithCourses.courses = courses;
//        studentWithCourses.student = student;

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
        startActivity(intent);
    }

    public void onAddCourseClicked(View view) {
        int newCourseID = db.coursesDao().count() + 1;
//        int studentID = student.getId();
        int studentID = 0;
        Spinner newQuarterTextView = findViewById(R.id.fidget_spinner);
        TextView newYearTextView = findViewById(R.id.input_year);
        TextView newSubjectTextView = findViewById(R.id.input_subject);
        TextView newCourseNumTextView = findViewById(R.id.input_course_number);
        String newQuarterText = newQuarterTextView.getSelectedItem().toString();
        int newYearText = Integer.parseInt(newYearTextView.getText().toString());
        String newSubjectText = newSubjectTextView.getText().toString();
        String newCourseNumText = newCourseNumTextView.getText().toString();

        Course newCourse = new Course(newCourseID, studentID, newYearText, newQuarterText, newSubjectText, newCourseNumText);
        db.coursesDao().insert(newCourse);

        coursesViewAdapter.addCourse(newCourse);
    }
}