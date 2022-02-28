package edu.ucsd.cse110.bof.viewProfile;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import edu.ucsd.cse110.bof.R;
import edu.ucsd.cse110.bof.model.db.Course;

public class CoursesListViewAdapter extends RecyclerView.Adapter<CoursesListViewAdapter.ViewHolder>{
    private final List<Course> courses;


    public CoursesListViewAdapter(List<Course> courses) {
        super();
        this.courses = courses;
    }

    @NonNull
    @Override
    public CoursesListViewAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater
                .from(parent.getContext())
                .inflate(R.layout.show_course_row, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CoursesListViewAdapter.ViewHolder holder, int position) {
        holder.setCourse(courses.get(position));
    }

    @Override
    public int getItemCount() {
        return this.courses.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        private final TextView courseTextView;
        private Course course;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            this.courseTextView = itemView.findViewById(R.id.course_info);

        }

        public void setCourse(Course course) {
            this.course = course;
            this.courseTextView.setText(course.toString());
        }

    }


}
