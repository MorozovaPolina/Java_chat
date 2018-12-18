package Client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.SocketChannel;

public class Client {
    private static final int PORT = 1234;
    private SocketChannel channel;
    private String name;

    public static void main(String[] args) {
        new Client().run();
    }

    public void run() {
        System.out.println("Привет, пользователь! Представься, пожалуйста.");
        try (BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(System.in))) {
            while (true)
                if (bufferedReader.ready()) {
                    name = bufferedReader.readLine();
                    System.out.println("Привет, " + name + "!");
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
                            switch (command){
                                case "quit":
                                    quit();
                                    watcher.interrupt();
                                    return;

                                case "online":
                                        getOnline();
                                    break;
                                default:
                                    String message = name + ": " + command;
                                    if(!message.equals(name+": ")) {
                                        //System.out.print(message);
                                        sendMessage(message);
                                    }
                                    break;

                            }

                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

            } catch (UnknownHostException e) {
                System.out.println("Хост не найден");
            } catch (IOException e) {
                e.printStackTrace();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
    public void sendMessage(String message){
        send(3, message.length(), message.getBytes());
    }

    public void introduce(){
        send(1, name.length(), name.getBytes());
    }

    public void quit(){
        send(5);
    }

    private void getOnline(){
        send(6);
    }

    public void send(int command){
        send(command, 0, new byte[0]);
    }

    public void send (int command, int length, byte[] data){
        ByteBuffer buffer = ByteBuffer.allocate(1024);
        buffer.putInt(command);
        buffer.putInt(length);
        buffer.put(data);
        buffer.flip();
        try {
            channel.write(buffer);
        } catch (IOException e) {
            e.getMessage();
        }
        buffer.clear();

    }
}
