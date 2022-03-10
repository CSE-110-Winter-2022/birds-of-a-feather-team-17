package edu.ucsd.cse110.bof;

import android.util.Log;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import edu.ucsd.cse110.bof.model.StudentWithCourses;
import edu.ucsd.cse110.bof.model.db.Course;
import edu.ucsd.cse110.bof.model.db.Student;


public class StudentWithCoursesBuilder implements IBuilder {

    private static final String TAG = "StudentWithCoursesBuilder";
    private static final String defaultURL = "https://commons.wikimedia" +
            ".org/wiki/File:Default_pfp.jpg";

    private String stuName;
    private String stuPhotoURL;
    private int stuID;
    private List<Course> stuCourses;

    public StudentWithCoursesBuilder() {
        reset();
    }

    @Override
    public IBuilder setStuName(String name) {
        Contract.REQUIRE(name != null && !name.equals(""),
                "name not null or empty");

        this.stuName = name;
        return this;
    }

    @Override
    public IBuilder setStuPhotoURL(String photoURL) {
        Contract.REQUIRE(photoURL != null, "photoURL not null");

        this.stuPhotoURL = (photoURL.equals("")) ? defaultURL : photoURL;
        return this;
    }

    @Override
    public IBuilder setStuID(int ID) {
        Contract.REQUIRE(ID >= 0,
                "valid id");

        this.stuID = ID;
        return this;
    }

    @Override
    public IBuilder addCourse(int year, String quarter, String subj, String number, String size) {
        int sizePre = stuCourses.size();

        Contract.REQUIRE(quarter != null && !quarter.equals(""),
                "quarter not null or empty");
        Contract.REQUIRE(subj != null && !subj.equals(""),
                "subject not null or empty");
        Contract.REQUIRE(number != null && !number.equals(""),
                "courseNum not null or empty");
        Contract.REQUIRE(size != null && !size.equals(""),
                "size not null or empty");

        this.stuCourses.add(new Course(-1, this.stuID, year, quarter, subj,
                number, size));

        Contract.ENSURE(stuCourses.size() == sizePre + 1,
                "list size incremented");

        return this;
    }

    @Override
    public IBuilder setCourses(List<Course> courses) {
        this.stuCourses = courses;
        return this;
    }

    @Override
    public IBuilder setFromCSV(String csv) {
        Log.d(TAG, "Within builder, reading csv: " + csv);
        Contract.REQUIRE(csv != null && !csv.equals(""),
                "csv not null or empty");

        Scanner reader = new Scanner(csv);
        reader.useDelimiter("\\s*[,\n]\\s*");

        setStuName(reader.next());
        reader.nextLine();

        setStuPhotoURL(reader.next());
        //reader.nextLine();

        ArrayList<Course> localCourses = new ArrayList<>();

        int year;
        String quarter, subject, courseNum;
        String courseSize;

        while (reader.hasNextLine()) {
            reader.nextLine();
            if (!reader.hasNext()) { break; }
            year = Integer.parseInt(reader.next());
            quarter = reader.next();
            subject = reader.next();
            courseNum = reader.next();
            courseSize = reader.next();

            localCourses.add(new Course(1, 1, year,
                    quarter, subject, courseNum, courseSize));
        }

        setCourses(localCourses);

        reader.close();

        return this;
    }

    @Override
    public StudentWithCourses getSWC() {
        Contract.REQUIRE(stuName != null && !stuName.equals(""),
                "stuName not null or empty");
        Contract.REQUIRE(stuPhotoURL != null && !stuPhotoURL.equals(""),
                "stuPhotoURL not null or empty");

        StudentWithCourses retVal = new StudentWithCourses(
                new Student(stuName, stuPhotoURL), stuCourses);

        reset();
        return retVal;
    }

    @Override
    public void reset() {
        this.stuName = null;
        this.stuPhotoURL = null;
        this.stuID = 0;
        this.stuCourses = new ArrayList<>();
        Contract.ENSURE(this.stuCourses.size() == 0, "new empty list");
    }
}
