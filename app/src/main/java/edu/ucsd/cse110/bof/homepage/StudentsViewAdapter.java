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
import android.widget.Toast;

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
import edu.ucsd.cse110.bof.model.IStudent;
import edu.ucsd.cse110.bof.model.db.AppDatabase;
import edu.ucsd.cse110.bof.model.db.ListConverter;
import edu.ucsd.cse110.bof.model.db.Student;
import edu.ucsd.cse110.bof.viewProfile.StudentDetailActivity;

/**
 * Adapter for turning a list of students into a RecyclerView
 */
public class StudentsViewAdapter extends RecyclerView.Adapter<StudentsViewAdapter.ViewHolder> {

    private static final String TAG = "StudentsViewAdapterLog";

    private List<Student> students;

    private final ExecutorService backgroundThreadExecutor =
            Executors.newSingleThreadExecutor();
    private Context context;
    private AppDatabase db;
    private String priority = "common classes"; //default sorting

    /**
     * Constructor that takes in a list of students
     * @param students a list of students
     */
    public StudentsViewAdapter(List<Student> students) {
        super();
        this.students = students;
    }

    /**
     * Returns a new ViewHolder given the passed in data
     * @param parent required for onCreateViewHolder
     * @param viewType required for onCreateViewHolder
     * @return ViewHolder
     */
    @NonNull
    @Override
    public StudentsViewAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater
                .from(parent.getContext())
                .inflate(R.layout.student_row, parent, false);

        return new ViewHolder(view, this.db, this.context, this);
    }

    /**
     * UI reactions to when the list updates
     * @param holder required for onBindViewHolder
     * @param position the position of the ViewHolder
     */
    @Override
    public void onBindViewHolder(@NonNull StudentsViewAdapter.ViewHolder holder, int position) {
        holder.setStudent(students.get(position));

        // Get the student photo from the internet
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

    /**
     * Adds a given student to the underlying list and notifies the UI to update
     * @param student the student to be added
     */
    public void addStudent(Student student) {

        Log.d(TAG, "adding student to viewAdapter");

        this.students.add(student);

        Log.d(TAG, "student added");

        this.notifyItemInserted(this.students.size()-1);

        Log.d(TAG, "notified RecyclerView that student was inserted");
    }

    /**
     * Clear all students from the list
     */
    public void clearStudents() {
        Log.d(TAG, "clearing all students from list...");

        this.students.clear();
        this.notifyDataSetChanged();
        Log.d(TAG, "list cleared");
    }

    /**
     * Sort the list based on priority selected and notify the UI to be updated
     * @param priority either sort by prioritizing # of common classes, small classes, or recency
     */
    public void sortList(String priority) {
        updateStudentList();
        if(priority == null || priority.equals("")) {
            priority = this.priority;
        }
        else {
            this.priority = priority;
        }
        if(priority.equals("common classes")) {
            students.sort((Comparator<IStudent>) (o1, o2) ->
                    Integer.compare(o2.getMatches() + o2.favMultiplier() + o2.waveMultiplier(), o1.getMatches() + o1.favMultiplier() + o1.waveMultiplier()));
        }
        else if (priority.equals("small classes")) {
            students.sort((Comparator<IStudent>) (o1, o2) ->
                    Float.compare(o2.getClassSizeWeight() + o2.favMultiplier() + o2.waveMultiplier(), o1.getClassSizeWeight() + o1.favMultiplier() + o1.waveMultiplier()));
        }
        else if (priority.equals("recent")) {
            students.sort((Comparator<IStudent>) (o1, o2) ->
                    Integer.compare(o2.getRecencyWeight() + o2.favMultiplier() + o2.waveMultiplier(), o1.getRecencyWeight() + o1.favMultiplier() + o1.waveMultiplier()));
        }
        this.notifyItemRangeChanged(0, students.size());
    }

    /**
     * Update the student list to show favorites and received waves
     */
    public void updateStudentList() {
        if(db != null) {
            Log.d(TAG, "students updated");
            for (Student i : students) {
                i.setIsFav(db.studentsDao().get(i.getStudentId()).getIsFav());
                i.setWavedAtMe(db.studentsDao().get(i.getStudentId()).isWavedAtMe());
                Log.d(TAG, "Student UUID: " + i.getUUID());
                Log.d(TAG, "Student name: " + i.getName());
                Log.d(TAG, "Student matches: " + i.getMatches());
                Log.d(TAG, "Student class size weight: " + i.getClassSizeWeight());
                Log.d(TAG, "Student recency weight: " + i.getRecencyWeight());
                Log.d(TAG, "Student is fav: " + i.getIsFav());
                Log.d(TAG, "Student waved at me: " + i.isWavedAtMe());
            }
        }
    }

    /**
     * Returns the number of students in the RecyclerView
     * @return the number of students shown
     */
    @Override
    public int getItemCount() {
        return this.students.size();
    }

    /**
     * Helper method for passing in the context
     * @param contextD the context to set
     */
    public void setContext(Context contextD) {
        this.context = contextD;
        this.db = AppDatabase.singleton(context);
        Log.d(TAG, "context set");
    }

    /**
     * Inner class to support populating and showing the student_row layout
     */
    public static class ViewHolder
            extends RecyclerView.ViewHolder implements View.OnClickListener {
        private final TextView studentNameView;
        private final TextView studentMatchesView;
        private final ImageView studentPhotoView;
        private final ImageView studentWaveIcon;

        private final ImageButton favButton;
        private final AppDatabase db;
        private final Context context;

        private IStudent student;

        /**
         * Constructor for showing the student_row layout
         * @param itemView required for viewHolder
         * @param db the database we are using
         * @param context required for sending toasts
         * @param sva to update the UI instantly upon clicking on the favorites button
         */
        public ViewHolder(View itemView, AppDatabase db, Context context, StudentsViewAdapter sva) {
            super(itemView);
            this.studentNameView = itemView.findViewById(R.id.student_row_name);
            this.studentMatchesView = itemView.findViewById(R.id.student_row_matches);
            this.studentPhotoView = itemView.findViewById(R.id.student_row_photo);
            this.studentWaveIcon = itemView.findViewById(R.id.wave_received_icon);
            this.db = db;
            this.context = context;
            this.favButton = itemView.findViewById(R.id.starButton);

            /**
             * Respond to setting a student as favorite or removing a student as favorite
             */
            this.favButton.setOnClickListener(view -> {
                boolean oppositeFav = !student.getIsFav();
                this.db.studentsDao().updateFav(student.getStudentId(), oppositeFav);

                // Add favorited student to Favorites Session, update database
                if (!student.getIsFav()) {
                    Toast.makeText(context, "Saved to Favorites", Toast.LENGTH_SHORT).show();
                    String updatedList = (db.sessionsDao().get(1).studentIDList) + "," + this.student.getStudentId();
                    db.sessionsDao().updateStudentList(1, updatedList);
                }
                // Remove unfavorited student
                else {
                    Toast.makeText(context, "Removed From Favorites", Toast.LENGTH_SHORT).show();
                    List<Integer> originalList = ListConverter.getListFromString(db.sessionsDao().get(1).studentIDList);
                    originalList.remove((Integer) this.student.getStudentId());
                    String updatedList = ListConverter.getStringFromList(originalList);
                    db.sessionsDao().updateStudentList(1, updatedList);
                }

                // Sort the list by currently set priority
                sva.sortList("");
                Log.d(TAG, "Favorite clicked");
            });

            itemView.setOnClickListener(this);
        }

        /**
         * Sets student information in the view given information received
         * @param student the student received
         */
        public void setStudent(IStudent student) {
            this.student = student;
            this.studentNameView.setText(student.getName());
            this.studentMatchesView.setText(String.format(US, "%d",
                    student.getMatches()));
            if(student.isWavedAtMe()) {
                this.studentWaveIcon.setVisibility(View.VISIBLE);
            }
            else {
                this.studentWaveIcon.setVisibility(View.GONE);
            }
            this.favButton.setImageResource(student.getIsFav() ? R.drawable.star_filled : R.drawable.star_hollow);
        }

        /**
         * Sets the student photo according to the bitmap parsed from the photo URL
         * @param photoBitmap the bitmap to set the PhotoView to
         */
        public void setPhoto(Bitmap photoBitmap) {
            this.studentPhotoView.setImageBitmap(photoBitmap);
            Log.d(TAG, "photo set");
        }

        /**
         * Pull up the student's details upon clicking on a student row
         * @param view required for onClick
         */
        @Override
        public void onClick(View view) {
            Context context = view.getContext();
            Intent intent = new Intent(context, StudentDetailActivity.class);

            intent.putExtra("student_id", this.student.getStudentId());
            context.startActivity(intent);
        }

        public ImageButton getFavButton() {
            return this.favButton;
        }
    }

    /**
     * Get the underlying students list
     * @return list of students
     */
    public List<Student> getStudents() {
        return this.students;
    }

    /**
     * Sets the underlying students list
     * @param students the desired list to set
     */
    public void setStudents(List<Student> students) { this.students = students; }
}