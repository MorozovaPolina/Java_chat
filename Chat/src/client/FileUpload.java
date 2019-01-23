package client;

import common.Command;

import java.io.IOException;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Thread for uploading file by client
 *
 * @author Anastasiia Chernysheva
 */
public class FileUpload implements Runnable {
    private final int INT_SIZE = 4;
    String fileName;
    InetSocketAddress inetSocketAddress;
    SocketChannel channel;
    String userName;

    public FileUpload(String fileName, InetSocketAddress inetSocketAddress, String name) {
        this.fileName = fileName;
        this.inetSocketAddress = inetSocketAddress;
        this.userName = name;

    }

    @Override
    public void run() {
        System.out.println("Start uploading file " + fileName);
        Path file = Paths.get(fileName);
        if (Files.exists(file)) {
            try (InputStream inputStream = Files.newInputStream(file)) {
                byte[] fileBytes = inputStream.readAllBytes();
                System.out.println("size of file is " + fileBytes.length + " bytes");
                channel = SocketChannel.open(inetSocketAddress);
                byte[] output = new byte[fileBytes.length + INT_SIZE + fileName.length() + INT_SIZE];
                System.out.println(fileName.length());
                System.arraycopy(ByteBuffer.allocate(INT_SIZE).
                        putInt(fileName.length()).array(), 0, output, 0, INT_SIZE);
                System.arraycopy(fileName.getBytes(), 0, output, INT_SIZE, fileName.length());
                System.arraycopy(ByteBuffer.allocate(INT_SIZE).
                        putInt(fileBytes.length).array(), 0, output, INT_SIZE + fileName.length(), INT_SIZE);
                System.arraycopy(fileBytes, 0, output, INT_SIZE + fileName.length() + INT_SIZE, fileBytes.length);
                SendData.send(channel, Command.UPLOAD_FILE, output.length, output);
                System.out.println("File " + fileName + " is uploaded succesfully");

            } catch (IOException e) {
                System.out.println("Some problems while uploading file");
                Thread.currentThread().interrupt();
            }
        } else {
            System.out.println("Could not find file " + fileName);
        }
        Thread.currentThread().interrupt();

    }


}
