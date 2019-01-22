package client;

import common.Command;

import java.io.IOException;
import java.net.StandardSocketOptions;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

public class SendData {

    /**
     * Send command with parameters to server
     *
     * @param channel channel
     * @param command command
     * @param length  data length
     * @param data    data
     */
    public static void send(SocketChannel channel, Command command, int length, byte[] data) throws IOException {
        ByteBuffer buffer = ByteBuffer.allocate(8 + 2 * length);
        buffer.putInt(command.getId());
        buffer.putInt(length);
        buffer.put(data);
        buffer.flip();

        channel.setOption(StandardSocketOptions.SO_SNDBUF, 8 + 2 * length);
        channel.write(buffer);

        buffer.clear();
    }
}
