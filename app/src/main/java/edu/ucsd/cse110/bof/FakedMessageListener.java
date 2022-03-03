package edu.ucsd.cse110.bof;
import android.app.Activity;
import android.content.Context;
import android.util.Log;

import com.google.android.gms.nearby.messages.Message;
import com.google.android.gms.nearby.messages.MessageListener;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.nio.charset.StandardCharsets;

import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import edu.ucsd.cse110.bof.model.IStudent;

import edu.ucsd.cse110.bof.model.db.Course;
import edu.ucsd.cse110.bof.model.db.Student;

public class FakedMessageListener extends MessageListener {
    //defines behavior on receiving new Message
    private final MessageListener messageListener;

    //for running on bg thread
    private final ScheduledExecutorService executor;

    //for stopping the background thread
    private ScheduledFuture<Void> future;

    private Context context;

    //for logging
    private static final String TAG = "FakedMessageListenerLog";

    //mocks receiving StudentWithCourses as message at given frequency
    public FakedMessageListener(MessageListener realMessageListener,
                                int frequency,
                                StudentWithCourses studentWithCourses,
                                Context context) {
        this.messageListener = realMessageListener;
        this.executor = Executors.newSingleThreadScheduledExecutor();

        //make byte array for student and courses
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ObjectOutputStream out;
        byte[] studentWithCoursesBytes = new byte[0];
        try {
            out = new ObjectOutputStream(bos);
            out.writeObject(studentWithCourses);
            out.flush();
            studentWithCoursesBytes = bos.toByteArray();
            bos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        byte[] finalStudentWithCoursesBytes = studentWithCoursesBytes;


/*
        executor.scheduleAtFixedRate(() -> {

            Log.d(TAG, "sending mocked message");
            Message message = new
                    Message(finalStudentWithCoursesBytes);
            this.messageListener.onFound(message);
            this.messageListener.onLost(message);
        }, 0, frequency, TimeUnit.SECONDS);
 */

        this.context = context;

        this.future = executor.schedule(() -> {
            Log.d(TAG, "sending mocked message");
            Message message = new
                    Message(finalStudentWithCoursesBytes);
            this.messageListener.onFound(message);

            return null;
        }, 0, TimeUnit.SECONDS);

        /*
        Log.d(TAG, "sending mocked message");
        Message message = new
                Message(finalStudentWithCoursesBytes);
        this.messageListener.onFound(message);

         */
    }


    //stops executor from running
    public void stopRun() {
        try {
            future.get();
        } catch (ExecutionException | InterruptedException e) {
            e.printStackTrace(System.err);
        }
        this.future.cancel(true);
    }
}