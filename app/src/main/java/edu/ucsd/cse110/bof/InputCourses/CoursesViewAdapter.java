package edu.ucsd.cse110.bof.InputCourses;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;
import java.util.function.Consumer;

import edu.ucsd.cse110.bof.R;
import edu.ucsd.cse110.bof.model.db.Course;

public class CoursesViewAdapter extends RecyclerView.Adapter<CoursesViewAdapter.ViewHolder>{
    private List<Course> courses;
    private Consumer<Course> onCourseRemoved;

    public CoursesViewAdapter(List<Course> courses, Consumer<Course> onCourseRemoved) {
        super();
        this.courses = courses;
        this.onCourseRemoved = onCourseRemoved;
    }

    @NonNull
    @Override
    public CoursesViewAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater
                .from(parent.getContext())
                .inflate(R.layout.input_course_row, parent, false);

        return new ViewHolder(view, this::removeCourse, onCourseRemoved);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.setCourse(courses.get(position));
    }

    @Override
    public int getItemCount() {
        return this.courses.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        private final TextView courseTextView;
        private Course course;

        public ViewHolder(@NonNull View itemView, Consumer<Integer> removeCourse, Consumer<Course> onCourseRemoved) {
            super(itemView);
            this.courseTextView = itemView.findViewById(R.id.course_info);

            Button removeButton =itemView.findViewById(R.id.remove_course_button);
            removeButton.setOnClickListener((view) -> {
                removeCourse.accept(this.getAdapterPosition());
                onCourseRemoved.accept(course);
            });
        }

        public void setCourse(Course course) {
            this.course = course;
            this.courseTextView.setText(course.toString());
        }
    }

    public void addCourse(Course course) {
        this.courses.add(course);
        this.notifyItemInserted(this.courses.size() - 1);
    }

    public void removeCourse(int position) {
        this.courses.remove(position);
        this.notifyItemRemoved(position);
    }
}
