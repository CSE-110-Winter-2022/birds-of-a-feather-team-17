package edu.ucsd.cse110.bof;
import com.google.android.gms.nearby.messages.Message;
import com.google.android.gms.nearby.messages.MessageListener;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;

import edu.ucsd.cse110.bof.model.StudentWithCourses;

public class FakedMessageListener extends MessageListener {
    private final MessageListener messageListener;

    //mocks receiving StudentWithCourses as message at given frequency
    public FakedMessageListener(MessageListener realMessageListener,
                                int frequency, StudentWithCourses studentWithCourses) {
        this.messageListener = realMessageListener;

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

        Message message = new
                Message(finalStudentWithCoursesBytes);
        this.messageListener.onFound(message);
        this.messageListener.onLost(message);
    }
}