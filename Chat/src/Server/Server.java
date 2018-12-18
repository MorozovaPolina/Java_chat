package Server;

import javax.swing.*;
import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;

public class Server {

    private static final int PORT = 1234;
    private Selector selector;
    private ArrayList<String> messages;
    private HashMap<SocketAddress, String> connectedClients;


    public static void main (String[] args) {
    new Server().run();
    }

    public void run(){
        try {
            selector = Selector.open();
            messages = new ArrayList<>();
            connectedClients = new HashMap<>();
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
        client.register(selector, SelectionKey.OP_READ);
    }

    /*Функция для обработки команды, пришедшей от пользователя.
    * Коды:
    * 1 - передача информации о подключившемся пользователе
    * 2 - загрузка файлов
    * 3 - отправка сообщений
    * 4 - выгрузка файлов
    * 5 - завершение работы
    * 6 - получение информации о клиентах онлайн*/
    private void read(SelectionKey key) throws IOException {

        SocketChannel client = (SocketChannel) key.channel();
        int BUFFER_SIZE = 1024;
        ByteBuffer buffer = ByteBuffer.allocate(BUFFER_SIZE);
        client.read(buffer);
        System.out.println("buffer position "+ buffer.position());
        buffer.position(0);
        int command = buffer.getInt();
        System.out.println("Команда "+command);
        switch (command){
            case 1:
               processIntroduction(buffer, key);
                break;
            case 3:
                getMessage(buffer, key);
                break;
            case 5:
                processQuit(key);
                break;
            case 6: getOnlineClients(key);
        }
    }

    /*Функция для предоставления информации о подключившемся пользователе*/
    private void processIntroduction(ByteBuffer buffer, SelectionKey key){
        int size = buffer.getInt();
        byte[] input= new byte[size*2];
        buffer.get(input);
        String message = new String(input);
        connectedClients.put(((SocketChannel) key.channel()).socket().getRemoteSocketAddress(), message);
        broadcast(key, "Подключился пользователь "+message);
        sendMessageHistory(key);
    }

    /*Функция для отключения пользователя*/
    private void processQuit(SelectionKey key){
        String name = connectedClients.get(((SocketChannel) key.channel()).socket().getRemoteSocketAddress());
        if(name!=null) {
            connectedClients.remove(((SocketChannel) key.channel()).socket().getRemoteSocketAddress());
                key.cancel();
            try {
                key.channel().close();
            } catch (IOException e) {
                e.getMessage();
            }
            broadcast("Пользователь "+ name +" отключился" );


        }
    }

    /*Функция для получения информации о пользователях, которые сейчас онлайн*/
    public void getOnlineClients(SelectionKey key){
        StringBuffer message= new StringBuffer("Online: ");
        for(String name: connectedClients.values()) {
            message.append(name+", ");
        }
        message.delete(message.length()-2,message.length()-1 );
        sendMessage(key, message.toString());
    }

    /*Функция для получения сообщения от пользователя*/
    public void getMessage(ByteBuffer buffer, SelectionKey key){
        int size = buffer.getInt();
        byte[] input= new byte[size*2];
        buffer.get(input);
        String message = new String(input);
        messages.add(message);
        broadcast(key,message);
    }

    /*Функция для отправки сообщения пользователю*/
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

    /*broadcast используется для передачи сообщения всем клиентам*/
    private void broadcast(String message){
        System.out.println(message);
        for(SelectionKey key: selector.keys()) {
            sendMessage(key, message);
        }
    }

    /*broadcast используется для передачи сообщения всем клиентам, кроме источника сообщения*/
    private void broadcast(SelectionKey source, String message)  {
        System.out.println(message);
        for(SelectionKey key: selector.keys()) {
            if(!source.channel().equals(key.channel()))
            sendMessage(key, message);
        }
    }

    /*функция для отправки всех истории сообщений пользователю*/
    private void sendMessageHistory(SelectionKey key){
        for(String message: messages) sendMessage(key, message);
    }

}
