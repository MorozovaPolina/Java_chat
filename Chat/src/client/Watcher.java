package client;

import java.io.IOException;
import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Set;

/**
 * Thread that waits for messages from server
 * @author Polina Morozova
 */
public class Watcher implements Runnable {
    private final int BUFFER_SIZE = 1024;
    private SocketChannel channel;
    private Selector selector;
    public Watcher(SocketChannel channel){
        try {
            this.channel=channel;
            selector=Selector.open();
            channel.configureBlocking(false);
            channel.register(selector, SelectionKey.OP_READ);
        } catch (IOException e) {
            System.out.println("Error while trying to listen to the server messages");
        }


    }
    @Override
    public void run() {
        ByteBuffer buffer = ByteBuffer.allocate(BUFFER_SIZE);
        while (!Thread.currentThread().isInterrupted()) {
            try {
                int readyCount = selector.select();
                if (readyCount == 0) continue;
                Set<SelectionKey> readyKeys = selector.selectedKeys();
                Iterator iterator = readyKeys.iterator();
                while (iterator.hasNext()) {
                    SelectionKey key = (SelectionKey) iterator.next();
                    iterator.remove();
                    if (key.isReadable()) {
                        try {
                            channel.read(buffer);
                            buffer.position(0);
                            int size = buffer.getInt();
                            byte[] input = new byte[size];
                            try {
                                buffer.get(input);
                            }
                            catch (BufferUnderflowException e){
                                System.out.println("Message got from the server is too long");
                            }
                            System.out.print(new String(input, StandardCharsets.UTF_8));
                            buffer.clear();
                            Arrays.fill(buffer.array(), (byte) 0);
                        } catch (IOException e) {
                            Thread.currentThread().interrupt();
                            return;
                        }
                    }
                }
            } catch (IOException e) {
            System.out.println("error while getting to the server");
        }
        }
    }
}
