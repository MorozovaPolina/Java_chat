package server;

import common.Command;
import common.CommandNotFoundException;
import common.FileReader;

import java.io.IOException;
import java.io.InputStream;
import java.net.*;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

/**
 * Server logic
 *
 * @author Polina Morozova
 * @author Anastasiia Chernysheva
 */

public class Server {

    private static final int PORT = 1234;
    private Selector selector;
    private ArrayList<String> messages;
    private HashMap<SocketAddress, String> connectedClients;

    private static String SERVER_PACKAGE = "server";

    public static void main(String[] args) {
        new Server().run();
    }

    public void run() {
        try {
            selector = Selector.open();
            messages = new ArrayList<>();
            connectedClients = new HashMap<>();
            ServerSocketChannel serverChannel = ServerSocketChannel.open();
            serverChannel.configureBlocking(false);
            InetSocketAddress hostAddress = new InetSocketAddress(InetAddress.getLocalHost(), PORT);
            serverChannel.bind(hostAddress);
            serverChannel.register(selector, SelectionKey.OP_ACCEPT);
            while (true) {
                int readyCount = selector.select();
                if (readyCount == 0) continue;
                Set<SelectionKey> readyKeys = selector.selectedKeys();
                Iterator iterator = readyKeys.iterator();
                while (iterator.hasNext()) {
                    SelectionKey key = (SelectionKey) iterator.next();
                    iterator.remove();
                    if (!key.isValid()) continue;
                    if (key.isAcceptable()) {
                        try {
                            accept(key);
                        } catch (IOException e) {
                            System.out.println("Error while trying to connect to the client");
                            processQuit(key);
                        }
                    }
                    if (key.isReadable()) {
                        try {
                            read(key);
                        } catch (IOException e) {
                            System.out.println("Error while trying to get information from the client");
                            processQuit(key);
                        }
                    }

                }
            }
        } catch (IOException e) {
            e.getMessage();
        }

    }

    /**
     * Method accepts a client connection
     *
     * @param key client
     * @throws IOException thrown if something went wrong during connection
     */

    public void accept(SelectionKey key) throws IOException {
        ServerSocketChannel server = (ServerSocketChannel) key.channel();
        SocketChannel client = server.accept();
        client.configureBlocking(false);
        Socket socket = client.socket();
        SocketAddress socketAddress = socket.getRemoteSocketAddress();
        System.out.println("Connected to " + socketAddress);
        client.register(selector, SelectionKey.OP_READ);
    }

    /**
     * Method to process the command from the client. Each command has a unique key.
     * 1 - information about a client
     * 2 - file uploading
     * 3 - message sending
     * 4 - file downloading
     * 5 - exit
     * 6 - getting information about clients
     *
     * @param key client key
     * @throws IOException thrown if something went wrong during getting information from client
     */
    public void read(SelectionKey key) throws IOException {

        SocketChannel client = (SocketChannel) key.channel();
        int BUFFER_SIZE = 1024;
        ByteBuffer buffer = ByteBuffer.allocate(BUFFER_SIZE);
        client.read(buffer);
        buffer.position(0);
        int commandInteger = buffer.getInt();

        Command command = Command.getById(commandInteger);
        switch (command) {
            case INTRODUCE:
                processIntroduction(buffer, key);
                break;
            case UPLOAD_FILE:
                getFile(buffer, key);
                break;
            case MESSAGE:
                getMessage(buffer, key);
                break;
            case DOWNLOAD_FILE:
                sendFile(buffer, key);
                break;
            case QUIT:
                processQuit(key);
                break;
            case GET_ONLINE:
                getOnlineClients(key);
                break;
            case MESSAGE_HISTORY:
                sendMessageHistory(key);
                break;
            default:
                throw new AssertionError("Unsupported command " + command);
        }

    }

    /**
     * Gets file from user
     * Method reads file, which is uploaded by user and writes it to local server package.
     *
     * @param buffer command info from user
     * @param key    key for client chanel identification
     */
    public void getFile(ByteBuffer buffer, SelectionKey key) {
        String fileName = "";
        try {

            // skip full buffer size
            buffer.getInt();

            int fileNameSize = buffer.getInt();
            byte[] fileNameInput = new byte[fileNameSize];
            buffer.get(fileNameInput);
            fileName = new String(fileNameInput, StandardCharsets.UTF_8);
            int size = buffer.getInt();

            byte[] input = new byte[buffer.limit() - buffer.position()];
            byte[] full_input;

            buffer.get(input);

            if (size > 1024) {
                SocketChannel client = (SocketChannel) key.channel();
                byte[] input1 = FileReader.readFile(client, size);

                full_input = FileReader.concatByteArrays(input, input1, size);

            } else {
                full_input = new byte[size];
                System.arraycopy(input, 0, full_input, 0, size);
            }

            try {
                System.out.println("Writes file " + fileName);
                if (!Files.exists(Paths.get(SERVER_PACKAGE))) {
                    Files.createDirectory(Paths.get(SERVER_PACKAGE));
                }
                Files.write(Paths.get(SERVER_PACKAGE + "/" + fileName), full_input);

                broadcast("Uploaded file " + fileName);
                messages.add("Uploaded file " + fileName);
            } catch (Exception e) {
                System.out.println("Problems occurred while writing file to server package: " + e);
            }


        } catch (Exception e) {
            System.out.println("Some problems while getting file from client: " + e);
        }


    }

    /**
     * Sends file to client by key.
     * Looks for fileName, get from user, in server package.
     * If there is no such file, generates message to user.
     *
     * @param buffer command from client
     * @param key    key for client chanel identification
     */
    public void sendFile(ByteBuffer buffer, SelectionKey key) {
        int size = buffer.getInt();
        byte[] input = new byte[size];
        buffer.get(input);
        String fileName = new String(input);

        String message = "";

        try {
            Path file = Paths.get(SERVER_PACKAGE + "/" + fileName);
            if (Files.exists(file)) {
                try (InputStream inputStream = Files.newInputStream(file)) {
                    byte[] fileBytes = inputStream.readAllBytes();

                    ByteBuffer bufferToSend = ByteBuffer.allocate(2 * fileBytes.length);
                    bufferToSend.putInt(1);

                    bufferToSend.putInt(fileBytes.length);
                    bufferToSend.put(fileBytes);
                    bufferToSend.flip();

                    if (key.isValid() && key.channel() instanceof SocketChannel) {
                        SocketChannel channel = (SocketChannel) key.channel();
                        channel.setOption(StandardSocketOptions.SO_SNDBUF, 2 * fileBytes.length);
                        channel.write(bufferToSend);
                        System.out.println();
                    }
                    buffer.clear();

                    System.out.println("File " + fileName + " is sent succesfully");
                } catch (IOException e) {
                    System.out.println("Some problems while sending file " + fileName);
                    message = "Could not send file " + fileName;
                }
            } else {
                message = "Could not find file " + fileName;
            }
        } catch (InvalidPathException e) {
            message = "Could not find file " + fileName;
        }
        if (!message.isEmpty()) {
            sendMessage(key, message);
        }

    }

    /**
     * This method allows server to get information about connected client's name.
     *
     * @param buffer command from client
     * @param key    key for client chanel identification
     */
    public void processIntroduction(ByteBuffer buffer, SelectionKey key) {
        String message = getMessage(buffer);

        connectedClients.put(((SocketChannel) key.channel()).socket().getRemoteSocketAddress(), message);
        broadcast(key, "User " + message + " has connected");
        sendMessageHistory(key);
    }

    /**
     * This method allows to disconnect a client
     *
     * @param key key for client chanel identification
     */
    public void processQuit(SelectionKey key) {
        String name = connectedClients.get(((SocketChannel) key.channel()).socket().getRemoteSocketAddress());
        if (name != null) {
            connectedClients.remove(((SocketChannel) key.channel()).socket().getRemoteSocketAddress());
            key.cancel();
            try {
                key.channel().close();
            } catch (IOException e) {
                e.getMessage();
            }
            broadcast("User " + name + " disconnected");


        }
    }

    /**
     * This method provides with the information about connected clients
     *
     * @param key key for client chanel identification
     */
    public void getOnlineClients(SelectionKey key) {
        StringBuffer message = new StringBuffer("Online: ");
        for (String name : connectedClients.values()) {
            message.append(name + ", ");
        }
        message.delete(message.length() - 2, message.length() - 1);
        sendMessage(key, message.toString());
    }

    /**
     * This method processes clients' text messages
     *
     * @param buffer command from client
     * @param key    key for client chanel identification
     */
    public void getMessage(ByteBuffer buffer, SelectionKey key) {
        String message = getMessage(buffer);
        messages.add(message);
        broadcast(key, message);
    }

    /**
     * Method for sending a message to the client
     *
     * @param key     client
     * @param message text message
     */
    public void sendMessage(SelectionKey key, String message) {
        ByteBuffer buffer = ByteBuffer.allocate(1024);
        buffer.putInt(message.length());
        buffer.put(message.getBytes());
        buffer.flip();
        if (key.isValid() && key.channel() instanceof SocketChannel) {
            SocketChannel channel = (SocketChannel) key.channel();
            try {
                channel.write(buffer);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        buffer.clear();
    }

    /**
     * Method for broadcast message sending
     *
     * @param message message
     */
    public void broadcast(String message) {
        System.out.println(message);
        for (SelectionKey key : selector.keys()) {
            sendMessage(key, message);
        }
    }

    /**
     * Method for sending a message to all connected users except the message source
     *
     * @param source  client who sent the message
     * @param message message
     */
    public void broadcast(SelectionKey source, String message) {
        System.out.println(message);
        for (SelectionKey key : selector.keys()) {
            if (!source.channel().equals(key.channel()))
                sendMessage(key, message);
        }
    }

    /**
     * Method for message history sending to a client
     *
     * @param key client
     */
    public void sendMessageHistory(SelectionKey key) {
        for (String message : messages) sendMessage(key, message);
    }

    private String getMessage(ByteBuffer buffer) {
        int size = buffer.getInt();
        byte[] input = new byte[size * 2];
        buffer.get(input);
        String message = new String(input);
        return message;
    }
}
