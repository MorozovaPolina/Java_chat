package client;

import common.Command;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.nio.channels.SocketChannel;
import java.util.NoSuchElementException;
import java.util.Scanner;

/**
 * Client main thread that processes user's commands
 *
 * @author Polina Morozova
 * @author Anastasiia Chernysheva
 */
public class Client {

    private static final int PORT = 1234;
    private final int MAX_MESSAGE_SIZE=500;
    private SocketChannel channel;
    private String name;

    public static void main(String[] args) {
        new Client().run();
    }

    public void run() {
        System.out.println("Hello, user! Please, introduce yourself.");
        try (BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(System.in))) {
            Scanner scanner = new Scanner(bufferedReader);
            name = scanner.next();
            System.out.println("Hello, " + name + "!");
            try {
                InetSocketAddress inetSocketAddress = new InetSocketAddress(InetAddress.getLocalHost(), PORT);
                channel = SocketChannel.open(inetSocketAddress);
                Thread watcher = new Thread(new Watcher(channel));
                watcher.start();
                introduce();

                while (true) {
                    try {
                            String command = scanner.nextLine();
                            String commandType = command.split(" ")[0];
                            switch (commandType) {
                                case "quit":
                                    quit();
                                    watcher.interrupt();
                                    return;
                                case "online":
                                    getOnline();
                                    break;
                                case "messages":
                                    getMessages();
                                    break;
                                case "upload":
                                    String fileName = command.split(" ")[1];
                                    uploadFile(fileName, inetSocketAddress, name);
                                    break;
                                case "download":
                                    String fileNameDownload = command.split(" ")[1];
                                    downloadFile(fileNameDownload, inetSocketAddress, name);
                                    break;
                                default:
                                    String message = name + ": " + command;
                                    if (!(name + ": ").equals(message)) {
                                        sendMessage(message);
                                    }
                                    break;

                            }

                    } catch (ArrayIndexOutOfBoundsException e) {
                        System.out.println("Missed some parameters.");
                    }
                }

            } catch (UnknownHostException e) {
                System.out.println("Host is not found");
            }
        } catch (IOException e) {
            System.out.println("Unexpected error");
        }
        catch (NoSuchElementException e){

        }

    }

    /**
     * Start file download process
     *
     * @param fileName          name of file to download
     * @param inetSocketAddress server address
     * @param name              name of user, who want to download file
     */
    public void downloadFile(String fileName, InetSocketAddress inetSocketAddress, String name) {
        Thread fileDownloadThread = new Thread(new FileDownload(fileName, inetSocketAddress, name));
        fileDownloadThread.start();
    }

    /**
     * Start file upload process
     *
     * @param fileName          name of file to upload
     * @param inetSocketAddress server address
     * @param name              name of user, who want to upload file
     */
    public void uploadFile(String fileName, InetSocketAddress inetSocketAddress, String name) {
        Thread fileUploadThread = new Thread(new FileUpload(fileName, inetSocketAddress, name));
        fileUploadThread.start();
    }

    /**
     * Get all messages
     */
    public void getMessages() {
        send(Command.MESSAGE_HISTORY);
    }

    /**
     * Send message to server
     *
     * @param message message
     */
    public void sendMessage(String message)  {
        try {
            if(message.length()>MAX_MESSAGE_SIZE)System.out.println("Message is too long.");
            SendData.send(channel, Command.MESSAGE, message.length(), message.getBytes());
        } catch (IOException e) {
            System.out.println("Error while sending a message");
        }
    }

    /**
     * Inform serer about client's existence
     */
    public void introduce() {
        try {
            SendData.send(channel, Command.INTRODUCE, name.length(), name.getBytes());
        } catch (IOException e) {
            System.out.println("Error while introducing");
        }
    }

    /**
     * Stop session
     */
    public void quit() {
        send(Command.QUIT);
    }

    /**
     * Get information about online users
     */
    public void getOnline(){
        send(Command.GET_ONLINE);
    }

    /**
     * Send client command to server
     *
     * @param command command
     */
    public void send(Command command)  {
        try {
            SendData.send(channel, command, 0, new byte[0]);
        } catch (IOException e) {
            System.out.println("Error while sending data");
        }
    }


}
