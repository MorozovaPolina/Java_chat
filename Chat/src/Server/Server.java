package Server;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Set;

public class Server {

    private static final int PORT = 1234;
    private Selector selector;
    private ArrayList<String> messages;


    public static void main (String[] args) {
    new Server().run();
    }

    public void run(){
        try {
            selector = Selector.open();
            messages = new ArrayList<>();
            ServerSocketChannel serverChannel = ServerSocketChannel.open();
            serverChannel.configureBlocking(false);
            InetSocketAddress hostAddress = new InetSocketAddress(InetAddress.getLocalHost(), PORT);
            serverChannel.bind(hostAddress);
            serverChannel.register(selector, SelectionKey.OP_ACCEPT);
            while (true){
                int readyCount=selector.select();
                if(readyCount == 0) continue;
                Set<SelectionKey> readyKeys = selector.selectedKeys();
                Iterator iterator = readyKeys.iterator();
                while (iterator.hasNext()){
                    SelectionKey key = (SelectionKey) iterator.next();
                    iterator.remove();
                    if(!key.isValid())continue;
                    if(key.isAcceptable()) accept(key);
                    if(key.isReadable())  read(key);
                   // if(key.isWritable()) readers.add(key);

                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private void accept (SelectionKey key) throws IOException {
        ServerSocketChannel server = (ServerSocketChannel) key.channel();
        SocketChannel client = server.accept();
        client.configureBlocking(false);
        Socket socket = client.socket();
        SocketAddress socketAddress = socket.getRemoteSocketAddress();
        System.out.println("Подключено к "+socketAddress);
        sendMessageHistory(key);
        client.register(selector, SelectionKey.OP_READ);
    }

    private void read(SelectionKey key) throws IOException {
        SocketChannel client = (SocketChannel) key.channel();
        int BUFFER_SIZE = 1024;
        ByteBuffer buffer = ByteBuffer.allocate(BUFFER_SIZE);
        client.read(buffer);
        String message = new String(buffer.array());
        messages.add(message);
        broadcast(key,message);

    }

    private void sendMessage(SelectionKey key, String message){
        ByteBuffer buffer = ByteBuffer.allocate(1024);
        buffer.put(message.getBytes());
        buffer.flip();
        if(key.isValid()&& key.channel() instanceof SocketChannel) {
            SocketChannel channel = (SocketChannel) key.channel();
            try {
                channel.write(buffer);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        buffer.clear();

    }
    /*broadcast используется для передачи собщения всем клиентам*/
    private void broadcast(String message) throws IOException {
        System.out.println(message);
        for(SelectionKey key: selector.keys()) {
            sendMessage(key, message);
        }
    }

    /*broadcast используется для передачи ссобщения всем клиентам, кроме источника сообщения*/
    private void broadcast(SelectionKey source, String message) throws IOException {
        System.out.println(message);
        for(SelectionKey key: selector.keys()) {
            if(!source.channel().equals(key.channel()))
            sendMessage(key, message);
        }
    }
    /*функция для отправки всех сохраненных сообщений*/
    private void sendMessageHistory(SelectionKey key){
        for(String message: messages) sendMessage(key, message);

    }

}
