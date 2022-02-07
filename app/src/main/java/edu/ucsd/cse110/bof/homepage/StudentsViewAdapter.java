package edu.ucsd.cse110.bof.homepage;

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
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.Objects;

import edu.ucsd.cse110.bof.R;
import edu.ucsd.cse110.bof.model.IStudent;

public class StudentsViewAdapter extends RecyclerView.Adapter<StudentsViewAdapter.ViewHolder>{

    private final List<? extends IStudent> students;

    public StudentsViewAdapter(List<? extends IStudent> students) {
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

    @Override
    public int getItemCount() {
        return this.students.size();
    }

    public static class ViewHolder
            extends RecyclerView.ViewHolder {
        //implements View.OnClickListener {
        private final TextView studentNameView;
        private final TextView studentMatchesView;
        private final ImageView studentPhotoView;
        private IStudent student;

        ViewHolder(View itemView) {
            super(itemView);
            this.studentNameView = itemView.findViewById(R.id.student_row_name);
            this.studentMatchesView = itemView.findViewById(R.id.student_row_matches);
            this.studentPhotoView = itemView.findViewById(R.id.student_row_photo);
            //itemView.setOnClickListener(this);
        }

        public void setStudent(IStudent student) {
            this.student = student;
            this.studentNameView.setText(student.getName());
            this.studentMatchesView.setText(student.getMatches());

            URL photo_url = null;
            try {
                photo_url = new URL(student.getPhotoUrl());
            } catch (MalformedURLException e) {
                e.printStackTrace();
            }
            Bitmap photo_bmp = null;
            try {
                photo_bmp = BitmapFactory.decodeStream(Objects.requireNonNull(photo_url).openConnection().getInputStream());
            } catch (IOException e) {
                e.printStackTrace();
            }

            this.studentPhotoView.setImageBitmap(photo_bmp);
        }

//        @Override
//        public void onClick(View view) {
//            Context context = view.getContext();
//            Intent intent = new Intent(context, PersonDetailActivity.class);
//            //intent.putExtra("student_name", this.student.getName());
//            //intent.putExtra("student_notes", this.student.getNotes().toArray(new String[0]));
//
//            intent.putExtra("student_id", this.student.getId());
//            context.startActivity(intent);
//        }
    }
}
