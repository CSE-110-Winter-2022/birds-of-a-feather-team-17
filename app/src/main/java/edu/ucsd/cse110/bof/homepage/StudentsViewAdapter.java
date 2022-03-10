package edu.ucsd.cse110.bof.homepage;

import static java.util.Locale.US;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.net.ssl.HttpsURLConnection;

import edu.ucsd.cse110.bof.R;
import edu.ucsd.cse110.bof.model.IStudent;
import edu.ucsd.cse110.bof.model.db.AppDatabase;
import edu.ucsd.cse110.bof.model.db.Student;
import edu.ucsd.cse110.bof.viewProfile.StudentDetailActivity;

public class StudentsViewAdapter extends RecyclerView.Adapter<StudentsViewAdapter.ViewHolder> {

    private static final String TAG = "StudentsViewAdapterLog";

    private List<Student> students;

    private final ExecutorService backgroundThreadExecutor =
            Executors.newSingleThreadExecutor();
    private Context context;
    private AppDatabase db;
    private String priority = "common classes";

    public StudentsViewAdapter(List<Student> students) {
        super();
        this.students = students;
    }

    @NonNull
    @Override
    public StudentsViewAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater
                .from(parent.getContext())
                .inflate(R.layout.student_row, parent, false);

        return new ViewHolder(view, this.db, this);
    }

    @Override
    public void onBindViewHolder(@NonNull StudentsViewAdapter.ViewHolder holder, int position) {
        holder.setStudent(students.get(position));
        backgroundThreadExecutor.submit(() -> {
            Log.d(TAG, "retrieving Student " + students.get(position).getName() +
                    "'s photo from internet...");
            URL photo_url = null;
            try {
                photo_url = new URL(students.get(position).getPhotoUrl());
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
            Log.d(TAG, "photo retrieved: " + photoBitmap);

            final Bitmap finalPhotoBitmap = photoBitmap;
            ((Activity)context).runOnUiThread(() -> {
                Log.d(TAG, "setting holder's photo...");
                holder.setPhoto(finalPhotoBitmap);
            });
        });
    }

    //called from HomePageActivity when the list of students is updated
    public void addStudent(Student student) {

        Log.d(TAG, "adding student to viewAdapter");

        this.students.add(student);

        Log.d(TAG, "student added");

        this.notifyItemInserted(this.students.size()-1);

        Log.d(TAG, "notified RecyclerView that student was inserted");
    }

    //sort the students list by specified priority algorithm
    public void sortList(String priority) {
        updateStudentList();
        if(priority == null || priority == "") {
            priority = this.priority;
        }
        switch (priority) {
            case "recent":
                this.priority = "recent";
                students.sort((s1, s2) -> {
                    if (s1.getIsFav() ^ s2.getIsFav()) {
                        return (s1.getIsFav()) ? -1 : 1;
                    } else {
                        return Integer.compare(s2.getRecencyWeight(), s1.getRecencyWeight());
                    }
                });
                break;
            case "small classes":
                this.priority = "small classes";
                students.sort((s1, s2) -> {
                    if (s1.getIsFav() ^ s2.getIsFav()) {
                        return (s1.getIsFav()) ? -1 : 1;
                    } else {
                        return Float.compare(s2.getClassSizeWeight(), s1.getClassSizeWeight());
                    }
                });
                break;
            case "common classes":
                this.priority = "common classes";
                students.sort((s1, s2) -> {
                    if (s1.getIsFav() ^ s2.getIsFav()) {
                        return (s1.getIsFav()) ? -1 : 1;
                    } else {
                        return Integer.compare(s2.getMatches(), s1.getMatches());
                    }
                });
                break;
        }
        this.notifyItemRangeChanged(0, students.size());
    }

    public void updateStudentList() {
        if(db != null) {
            Log.d(TAG, "students updated");
            for(Student i : students) {
                i.setIsFav(db.studentsDao().get(i.getStudentId()).getIsFav());
                Log.d(TAG, "Student name: " + i.getName());
                Log.d(TAG, "Student matches: " + i.getMatches());
                Log.d(TAG, "Student class size weight: " + i.getClassSizeWeight());
                Log.d(TAG, "Student recency weight: " + i.getRecencyWeight());
                Log.d(TAG, "Student is fav: " + i.getIsFav());
            }
        }
    }

    @Override
    public int getItemCount() {
        return this.students.size();
    }

    public void setContext(Context contextD) {
        this.context = contextD;
        this.db = AppDatabase.singleton(context);
        Log.d(TAG, "context set");
    }

    public static class ViewHolder
            extends RecyclerView.ViewHolder implements View.OnClickListener {
        private final TextView studentNameView;
        private final TextView studentMatchesView;
        private final ImageView studentPhotoView;
        private final ImageButton favButton;
        private final AppDatabase db;

        private IStudent student;

        public ViewHolder(View itemView, AppDatabase db, StudentsViewAdapter sva) {
            super(itemView);
            this.studentNameView = itemView.findViewById(R.id.student_row_name);
            this.studentMatchesView = itemView.findViewById(R.id.student_row_matches);
            this.studentPhotoView = itemView.findViewById(R.id.student_row_photo);
            this.db = db;
            this.favButton = itemView.findViewById(R.id.starButton);
            this.favButton.setOnClickListener(view -> {
                this.db.studentsDao().delete((Student) student);
                student.setIsFav(!student.getIsFav());
                this.db.studentsDao().insert((Student) student);
                sva.sortList("");
                Log.d(TAG, "Favorite clicked");
            });

            itemView.setOnClickListener(this);
        }

        public void setStudent(IStudent student) {
            this.student = student;
            this.studentNameView.setText(student.getName());
            this.studentMatchesView.setText(String.format(US, "%d",
                    student.getMatches()));
            this.favButton.setImageResource(student.getIsFav() ? R.drawable.star_filled : R.drawable.star_hollow);
        }

        public void setPhoto(Bitmap photoBitmap) {
            this.studentPhotoView.setImageBitmap(photoBitmap);
            Log.d(TAG, "photo set");
        }

        @Override
        public void onClick(View view) {
            Context context = view.getContext();
            Intent intent = new Intent(context, StudentDetailActivity.class);

            intent.putExtra("student_id", this.student.getStudentId());
            context.startActivity(intent);
        }
    }

    //test method
    public List<Student> getStudents() {
        return this.students;
    }
}