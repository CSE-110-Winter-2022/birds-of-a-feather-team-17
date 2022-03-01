package edu.ucsd.cse110.bof;
import android.util.Log;

import com.google.android.gms.nearby.messages.Message;
import com.google.android.gms.nearby.messages.MessageListener;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.nio.charset.StandardCharsets;

import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import edu.ucsd.cse110.bof.model.IStudent;

import edu.ucsd.cse110.bof.model.db.Course;
import edu.ucsd.cse110.bof.model.db.Student;

public class FakedMessageListener extends MessageListener {
    private final MessageListener messageListener;
    private final ScheduledExecutorService executor;
    private static final String TAG = "FakedMessageListenerLog";

    //mocks receiving StudentWithCourses as message at given frequency
    public FakedMessageListener(MessageListener realMessageListener,
                                int frequency, StudentWithCourses studentWithCourses) {
        this.messageListener = realMessageListener;
        this.executor = Executors.newSingleThreadScheduledExecutor();

        //make byte array for student and courses
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ObjectOutputStream out = null;
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
        }, 0, frequency, TimeUnit.SECONDS);

         */

        executor.schedule(() -> {
            Log.d(TAG, "sending mocked message");
            Message message = new
                    Message(finalStudentWithCoursesBytes);
            this.messageListener.onFound(message);
        }, 0, TimeUnit.SECONDS);
    }
}