package edu.ucsd.cse110.bof.homepage;

import static java.util.Locale.US;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.net.ssl.HttpsURLConnection;

import edu.ucsd.cse110.bof.R;
import edu.ucsd.cse110.bof.StudentWithCourses;
import edu.ucsd.cse110.bof.model.IStudent;

import edu.ucsd.cse110.bof.viewProfile.StudentDetailActivity;
import edu.ucsd.cse110.bof.model.db.Student;


public class StudentsViewAdapter extends RecyclerView.Adapter<StudentsViewAdapter.ViewHolder> {

    private final List<IStudent> students;


    public StudentsViewAdapter(List<IStudent> students) {
        super();
        this.students = students;
    }

    @NonNull
    @Override
    public StudentsViewAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater
                .from(parent.getContext())
                .inflate(R.layout.student_row, parent, false);

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull StudentsViewAdapter.ViewHolder holder, int position) {
        holder.setStudent(students.get(position));
    }

    //called from HomePageActivity when the list of students is updated,
    //sort list based on numMatches, then update
    public void itemInserted(IStudent student) {
        students.add((Student) student);

        students.sort(new Comparator<IStudent>() {
            @Override
            //reverse order based on number of matching courses
            public int compare(IStudent o1, IStudent o2) {
                return Integer.compare(o2.getMatches(), o1.getMatches());
            }
        });

        this.notifyItemInserted(this.students.size() - 1);
    }

    @Override
    public int getItemCount() {
        return this.students.size();
    }

    public static class ViewHolder
            extends RecyclerView.ViewHolder implements View.OnClickListener {
        private final TextView studentNameView;
        private final TextView studentMatchesView;
        private final ImageView studentPhotoView;

        private ExecutorService backgroundThreadExecutor =
                Executors.newSingleThreadExecutor();

        private IStudent student;

        public ViewHolder(View itemView) {
            super(itemView);
            this.studentNameView = itemView.findViewById(R.id.student_row_name);
            this.studentMatchesView = itemView.findViewById(R.id.student_row_matches);
            this.studentPhotoView = itemView.findViewById(R.id.student_row_photo);
            itemView.setOnClickListener(this);
        }

        public void setStudent(IStudent student) {
            this.student = student;
            this.studentNameView.setText(student.getName());
            this.studentMatchesView.setText(String.format(US, "%d",
                    student.getMatches()));

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

                this.studentPhotoView.setImageBitmap(photoBitmap);
            });
        }


        @Override
        public void onClick(View view) {
           Context context = view.getContext();
            Intent intent = new Intent(context, StudentDetailActivity.class);
            intent.putExtra("student_id", this.student.getStudentId());
            context.startActivity(intent);
        }
    }
}