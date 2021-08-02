package cc.eumc.screenmirroringclient;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;

public class UdpClient {
    private DatagramSocket socket;
    private InetAddress address;
    private int port;

    public UdpClient(InetAddress address, int port) {
        this.address = address;
        this.port = port;
        try {
            this.socket = new DatagramSocket();
        } catch (SocketException e) {
            e.printStackTrace();
        }
    }

    public void send(byte[] data) throws IOException {
        DatagramPacket packet = new DatagramPacket(data, data.length, address, port);
        socket.send(packet);
    }

    public void close() {
        socket.close();
    }
}
