package edu.ucsd.cse110.bof;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;

public class studentWithCoursesBytesFactory {
    public static byte[] convert(StudentWithCourses swc)
    {
        //make byte array for student and courses
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ObjectOutputStream out = null;
        byte[] studentWithCoursesBytes = new byte[0];
        try {
            out = new ObjectOutputStream(bos);
            out.writeObject(swc);
            out.flush();
            studentWithCoursesBytes = bos.toByteArray();
            bos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return studentWithCoursesBytes;
    }
}
