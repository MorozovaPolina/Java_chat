package client;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

/**
 * Thread that waits for messages from server
 * @author Polina Morozova
 */
public class Watcher implements Runnable {
    SocketChannel channel;
    public Watcher(SocketChannel channel){
        this.channel=channel;
    }
    @Override
    public void run() {
        int BUFFER_SIZE = 1024;
        ByteBuffer buffer = ByteBuffer.allocate(BUFFER_SIZE);
        while (!Thread.currentThread().isInterrupted()) {

            try {
                channel.read(buffer);
                if (buffer.position()>0){
                    buffer.position(0);
                    int size = buffer.getInt();
                    byte[] input = new byte[size * 2];
                    buffer.get(input);
                    System.out.println(new String(input));
                    buffer.clear();
                    Arrays.fill(buffer.array(), (byte) 0);
                }

            }
            catch (IOException e) {
                Thread.currentThread().interrupt();
                return;
            }

        }
    }
}
