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

/**
 * Adapter for showing a list of entered courses in RecyclerView
 */
public class CoursesViewAdapter extends RecyclerView.Adapter<CoursesViewAdapter.ViewHolder>{
    private final List<Course> courses;
    private final Consumer<Course> onCourseRemoved;

    /**
     * Setting up the adapter to grab from a list of courses entered
     * @param courses
     * @param onCourseRemoved
     */
    public CoursesViewAdapter(List<Course> courses, Consumer<Course> onCourseRemoved) {
        super();
        this.courses = courses;
        this.onCourseRemoved = onCourseRemoved;
    }

    /**
     * Returns a new ViewHolder given the passed in data
     * @param parent required for onCreateViewHolder
     * @param viewType required for onCreateViewHolder
     * @return ViewHolder
     */
    @NonNull
    @Override
    public CoursesViewAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater
                .from(parent.getContext())
                .inflate(R.layout.input_course_row, parent, false);

        return new ViewHolder(view, this::removeCourse, onCourseRemoved);
    }

    /**
     * UI reactions to when the list updates
     * @param holder
     * @param position
     */
    @Override
    public void onBindViewHolder(@NonNull CoursesViewAdapter.ViewHolder holder, int position) {
        holder.setCourse(courses.get(position));
    }

    /**
     * Returns the number of courses in the RecyclerView
     * @return the number of courses shown
     */
    @Override
    public int getItemCount() {
        return this.courses.size();
    }

    /**
     * Inner class to support populating and showing the show_course_row layout
     */
    public static class ViewHolder extends RecyclerView.ViewHolder {
        private final TextView courseTextView;
        private Course course;

        /**
         * Constructor for showing the show_course_row layout
         * @param itemView required for ViewHolder
         * @param removeCourse allows a course to be removed
         * @param onCourseRemoved reacts to a course being removed
         */
        public ViewHolder(@NonNull View itemView, Consumer<Integer> removeCourse, Consumer<Course> onCourseRemoved) {
            super(itemView);
            this.courseTextView = itemView.findViewById(R.id.course_info);

            Button removeButton =itemView.findViewById(R.id.remove_course_button);
            removeButton.setOnClickListener((view) -> {
                removeCourse.accept(this.getAdapterPosition());
                onCourseRemoved.accept(course);
            });
        }

        /**
         * Setter for the underlying course list
         * @param course the underlying course list
         */
        public void setCourse(Course course) {
            this.course = course;
            this.courseTextView.setText(course.toString());
        }
    }

    /**
     * Inserts a course to the list, then notifies the UI to update
     * @param course the course to be inserted
     */
    public void addCourse(Course course) {
        this.courses.add(course);
        this.notifyItemInserted(this.courses.size() - 1);
    }

    /**
     * Deletes a course from the list, then notifies the UI to update
     * @param position the position of the course to be deleted
     */
    public void removeCourse(int position) {
        this.courses.remove(position);
        this.notifyItemRemoved(position);
    }
}
