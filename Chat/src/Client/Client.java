package Client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
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
                sendMessage("Подключился пользователь " + name);

                while (true) {
                    try {
                        if (bufferedReader.ready()) {
                            String message = name + ": " + bufferedReader.readLine();
                            if(!message.equals(name+": ")) {
                                //System.out.print(message);
                                sendMessage(message);
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
        try {
            ByteBuffer buffer = ByteBuffer.allocate(74);
            buffer.put(message.getBytes());
            buffer.flip();
            channel.write(buffer);
            buffer.clear();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}
