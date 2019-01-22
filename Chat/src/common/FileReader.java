package common;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

public class FileReader {
    public static byte[] readFile(SocketChannel channel, int size) throws IOException {
        byte[] input1 = new byte[size];
        ByteBuffer buffer1 = ByteBuffer.allocate(size);
        channel.read(buffer1);

        while (buffer1.position() < (buffer1.limit() - 1024)) {
            channel.read(buffer1);
        }

        buffer1.position(0);
        buffer1.get(input1);

        return input1;

    }

    public static byte[] concatByteArrays(byte[] array1, byte[] array2, int size){
        byte[] full_input = new byte[size];

        System.arraycopy(array1, 0, full_input, 0, array1.length);
        System.arraycopy(array2, 0, full_input, array1.length, size - array1.length);

        return full_input;
    }
}
