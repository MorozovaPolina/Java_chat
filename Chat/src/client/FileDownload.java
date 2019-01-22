package client;

import common.Command;
import common.FileReader;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * Thread for downloading file by client
 * @author Anastasiia Chernysheva
 */
public class FileDownload implements Runnable {
    String fileName;
    InetSocketAddress inetSocketAddress;
    SocketChannel channel;
    String userName;

    public FileDownload(String fileName, InetSocketAddress inetSocketAddress, String name) {
        this.fileName = fileName;
        this.inetSocketAddress = inetSocketAddress;
        this.userName = name;
    }

    @Override
    public void run() {
        System.out.println("Start downloading file " + fileName);
        try {
            channel = SocketChannel.open(inetSocketAddress);
            SendData.send(channel, Command.DOWNLOAD_FILE.getType(), fileName.length(), fileName.getBytes());

            int BUFFER_SIZE = 1024;
            ByteBuffer buffer = ByteBuffer.allocate(BUFFER_SIZE);
            while (!Thread.currentThread().isInterrupted()) {
                try {
                    channel.read(buffer);
                    buffer.position(0);
                    int success = buffer.getInt();
                    if (success != 1) {
                        System.out.println(new String(buffer.array(), StandardCharsets.UTF_8));
                        break;
                    } else {
                        int size = buffer.getInt();
                        if (size > 0) {
                            try {
                                byte[] input = new byte[1024 - 8];

                                byte[] full_input = new byte[size];
                                buffer.get(input);

                                if (size > 1024) {
                                    byte[] input1 = FileReader.readFile(channel, size);

                                    System.arraycopy(input, 0, full_input, 0, input.length);
                                    System.arraycopy(input1, 0, full_input, input.length, size - input.length);

                                } else {
                                    System.arraycopy(input, 0, full_input, 0, size);
                                }

                                if (!Files.exists(Paths.get(userName + "/"))) {
                                    Files.createDirectory(Paths.get(userName + "/"));
                                }
                                Files.write(Paths.get(fileName), full_input);
                                System.out.println("File " + fileName + " was downloaded succesfully");
                                break;

                            }  catch (Exception e) {
                                System.out.println("Something went wrong while reading file from channel: " + e);
                            }
                        }
                    }

                } catch (IOException e) {
                    System.out.println("Error occured while downloading file." + e.getMessage());
                    Thread.currentThread().interrupt();

                    return;
                }

            }

        } catch (IOException e) {
            System.out.println("Error occured while openning channel for downloading file. " + e.getMessage());
            e.printStackTrace();
        }


        Thread.currentThread().interrupt();
    }


}
