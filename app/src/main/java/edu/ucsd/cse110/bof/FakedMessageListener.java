package edu.ucsd.cse110.bof;
import com.google.android.gms.nearby.messages.Message;
import com.google.android.gms.nearby.messages.MessageListener;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import edu.ucsd.cse110.bof.model.IStudent;

public class FakedMessageListener extends MessageListener {
    private final MessageListener messageListener;
    private final ScheduledExecutorService executor;


    //mocks sending IStudent as message
    public FakedMessageListener(MessageListener realMessageListener,
                                IStudent student) {
        this.messageListener = realMessageListener;
        this.executor = Executors.newSingleThreadScheduledExecutor();

        //make byte array for student
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ObjectOutputStream out = null;
        byte[] studentBytes = new byte[0];
        try {
            out = new ObjectOutputStream(bos);
            out.writeObject(student);
            out.flush();
            studentBytes = bos.toByteArray();
            bos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        byte[] finalStudentBytes = studentBytes;

        executor.schedule(() -> {
            Message message = new
                    Message(finalStudentBytes);
            this.messageListener.onFound(message);
            this.messageListener.onLost(message);
        }, 0, TimeUnit.SECONDS);
    }
}