package client;

import common.Command;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

/**
 * Client main thread that processes user's commands
 *
 * @author Polina Morozova
 * @author Anastasiia Chernysheva
 */
public class Client {

    private static final int PORT = 1234;
    private SocketChannel channel;
    private String name;

    public static void main(String[] args) {
        new Client().run();
    }

    public void run() {
        System.out.println("Hello, user! Please, introduce yourself.");
        try (BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(System.in))) {
            while (true)
                if (bufferedReader.ready()) {
                    name = bufferedReader.readLine();
                    System.out.println("Hello, " + name + "!");
                    break;
                }
            try {
                InetSocketAddress inetSocketAddress = new InetSocketAddress(InetAddress.getLocalHost(), PORT);
                channel = SocketChannel.open(inetSocketAddress);
                Thread watcher = new Thread(new Watcher(channel));
                watcher.start();
                introduce();

                while (true) {
                    try {
                        if (bufferedReader.ready()) {
                            String command = bufferedReader.readLine();
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
                                default:
                                    String message = name + ": " + command;
                                    if (!message.equals(name + ": ")) {
                                        sendMessage(message);
                                    }
                                    break;

                            }

                        }
                    } catch (ArrayIndexOutOfBoundsException e) {
                        System.out.println("Missed some parameters.");
                    }
                }

            } catch (UnknownHostException e) {
                System.out.println("Host is not found");
            }
        } catch (IOException e) {
            e.printStackTrace();
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
    public void getMessages() throws IOException{
        send(Command.MESSAGE_HISTORY.getType());
    }

    /**
     * Send message to server
     *
     * @param message message
     */
    public void sendMessage(String message) throws IOException{
        SendData.send(channel, Command.MESSAGE.getType(), message.length(), message.getBytes());
    }

    /**
     * Inform serer about client's existence
     */
    public void introduce() throws IOException {
        SendData.send(channel, Command.INTRODUCE.getType(), name.length(), name.getBytes());
    }

    /**
     * Stop session
     */
    public void quit() throws IOException{
        send(Command.QUIT.getType());
    }

    /**
     * Get information about online users
     */
    public void getOnline()throws IOException {
        send(Command.GET_ONLINE.getType());
    }

    /**
     * Send client command to server
     *
     * @param command command
     */
    public void send(int command) throws IOException {
        SendData.send(channel, command, 0, new byte[0]);
    }


}
