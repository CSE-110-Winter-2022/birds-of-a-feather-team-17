package edu.ucsd.cse110.bof;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.test.core.app.ApplicationProvider;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

import java.util.ArrayList;
import java.util.List;

import edu.ucsd.cse110.bof.homepage.StudentsViewAdapter;
import edu.ucsd.cse110.bof.model.StudentWithCourses;
import edu.ucsd.cse110.bof.model.db.AppDatabase;
import edu.ucsd.cse110.bof.model.db.Course;
import edu.ucsd.cse110.bof.model.db.Session;
import edu.ucsd.cse110.bof.model.db.Student;

@RunWith(RobolectricTestRunner.class)
public class FavoritesTest {
    private AppDatabase db;
    private static int courseId = 1;
    private static int userId = 1;

    private List<Student> students = new ArrayList<>();
    private StudentWithCourses BobAndCourses;
    private StudentWithCourses CaseyAndCourses;
    private Context context;

    private static final String avaUUID = "a4ca50b6-941b-11ec-b9090242ac120002";
    private static final String someUUID1 = "232dc5a5-b428-4ff0-88af-8817afc8e098";
    private static final String someUUID2 = "7299ef8f-3b21-45d3-b105-f9ceddca48bf";

    private static final String bobPhoto = "https://upload.wikimedia" +
            ".org/wikipedia/en/c/c5/Bob_the_builder.jpg";
    private static final String caseyPhoto = "https://commons.wikimedia" +
            ".org/wiki/File:Default_pfp.jpg";

    //create Ava's courses
    private static final Course cse100FA22S_Ava = new Course(
            courseId++,
            userId,
            2022,
            "FA",
            "CSE",
            "100",
            "Small");
    private static final Course cse110WI22L_Ava = new Course(
            courseId++,
            userId,
            2022,
            "WI",
            "CSE",
            "110",
            "Large");

    private static final Course cse110WI22L_Bob = new Course(
            courseId++,
            2,
            2022,
            "WI",
            "CSE",
            "110",
            "Large");
    private static final Course cse210FA21S_Bob = new Course(
            courseId++,
            2,
            2021,
            "FA",
            "CSE",
            "210",
            "Small");

    private static final Course cse100FA22S_Casey = new Course(
            courseId++,
            3,
            2022,
            "FA",
            "CSE",
            "100",
            "Small");
    private static final Course cse110WI22L_Casey = new Course(
            courseId++,
            3,
            2022,
            "WI",
            "CSE",
            "110",
            "Large");

    @Before
    public void createDatabaseAndUser() {
        //Create db
        context = ApplicationProvider.getApplicationContext();
        db = AppDatabase.useTestSingleton(context);
        //db = AppDatabase.singleton(context);

        //create Ava (user) and insert her into db, then get her dbID
        Student Ava = new Student();
        Ava.setUUID(avaUUID);
        db.studentsDao().insert(Ava);
        Ava.setStudentId(db.studentsDao().maxId());

        //add Ava's courses to db
        db.coursesDao().insert(cse100FA22S_Ava);
        db.coursesDao().insert(cse110WI22L_Ava);

        //insert Bob and his courses into db:
        Student Bob = new Student("Bob", bobPhoto, someUUID1);
        db.studentsDao().insert(Bob);

        List<Course> bobCourses = new ArrayList<>();
        bobCourses.add(cse110WI22L_Bob);
        bobCourses.add(cse210FA21S_Bob);

        //add Bob's courses to db
        db.coursesDao().insert(cse110WI22L_Bob);
        db.coursesDao().insert(cse210FA21S_Bob);

        BobAndCourses = new StudentWithCourses(Bob, bobCourses, "");

        //add Casey to db
        Student Casey = new Student("Casey", caseyPhoto, someUUID2);
        db.studentsDao().insert(Casey);

        List<Course> caseyCourses = new ArrayList<>();
        caseyCourses.add(cse110WI22L_Casey);
        caseyCourses.add(cse100FA22S_Casey);

        //add Casey's courses to db
        db.coursesDao().insert(cse100FA22S_Casey);
        db.coursesDao().insert(cse110WI22L_Casey);

        CaseyAndCourses = new StudentWithCourses(Casey, caseyCourses, "");
    }

    // tests clicking the favorite icon on Bob's ViewHolder
    @Test
    public void testFavoriteBob() {
        RecyclerView srv = new RecyclerView(context);
        RecyclerView.LayoutManager slm = new LinearLayoutManager(context);

        StudentsViewAdapter sva = new StudentsViewAdapter(new ArrayList<>());
        srv.setAdapter(sva);
        srv.setLayoutManager(slm);

        db.sessionsDao().insert(new Session("", "", ""));

        sva.addStudent(BobAndCourses.getStudent());
        BobAndCourses.getStudent().setStudentId(2);
        sva.addStudent(CaseyAndCourses.getStudent());

        ViewGroup vg = new FrameLayout(context);

        View view = LayoutInflater
                .from(context)
                .inflate(R.layout.student_row, vg, false);

        StudentsViewAdapter.ViewHolder bobViewHolder =
                new StudentsViewAdapter.ViewHolder(view, db, context.getApplicationContext(), sva);
        bobViewHolder.setStudent(BobAndCourses.getStudent());

        bobViewHolder.getFavButton().callOnClick();

        Assert.assertTrue(db.studentsDao().get(2).getIsFav());
    }
}