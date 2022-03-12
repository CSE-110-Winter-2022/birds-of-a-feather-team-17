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

/**
 * Adapter for showing a list of courses in RecyclerView in StudentDetailActivity
 */
public class CoursesListViewAdapter extends RecyclerView.Adapter<CoursesListViewAdapter.ViewHolder>{
    private final List<Course> courses;

    /**
     * Setting up the adapter to grab from a list of courses found
     * @param courses the list of courses passed in
     */
    public CoursesListViewAdapter(List<Course> courses) {
        super();
        this.courses = courses;
    }

    /**
     * Returns a new ViewHolder given the passed in data
     * @param parent required for onCreateViewHolder
     * @param viewType required for onCreateViewHolder
     * @return ViewHolder
     */
    @NonNull
    @Override
    public CoursesListViewAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater
                .from(parent.getContext())
                .inflate(R.layout.show_course_row, parent, false);
        return new ViewHolder(view);
    }

    /**
     * UI reactions to when the list updates
     * @param holder required for onBindViewHolder
     * @param position position of ViewHolder
     */
    @Override
    public void onBindViewHolder(@NonNull CoursesListViewAdapter.ViewHolder holder, int position) {
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
         */
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            this.courseTextView = itemView.findViewById(R.id.course_info);

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


}
