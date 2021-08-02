package cc.eumc.screenmirroring.server;

import cc.eumc.screenmirroring.manager.MirrorManager;
import cc.eumc.screenmirroring.model.Mirror;
import cc.eumc.screenmirroring.util.NumericUtil;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

public class ScreenServer extends Thread {
    private final int port;
    private DatagramSocket socket;
    private boolean running = true;
    private byte[] buffer = new byte[1024];

    private MirrorManager mirrorManager;

    public ScreenServer(MirrorManager mirrorManager, int port) throws SocketException {
        this.port = port;
        this.mirrorManager = mirrorManager;
        this.socket = new DatagramSocket(port);
    }

    public void run() {
        while (running) {
            DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
            try {
                socket.receive(packet);
            } catch (IOException e) {
                e.printStackTrace();
                continue;
            }

            InetAddress address = packet.getAddress();
            int port = packet.getPort();
            packet = new DatagramPacket(buffer, buffer.length, address, port);
            byte[] received = packet.getData();

            /* Format:
            <2bytes: ID> | <6bytes: Password> | <1byte: Action> | <1015bytes: Data>
            0          1 | 2                7 | 8             8 | 9            1023
             */

            short id = NumericUtil.bytesToShort(Arrays.copyOfRange(received, 0, 2));
            System.out.printf("ID: %d%n", id);
            Mirror mirror = mirrorManager.getMirror(id);
            if (mirror == null) {
                continue;
            }

            String password = new String(Arrays.copyOfRange(received, 2, 8), StandardCharsets.US_ASCII);
            System.out.printf("PW: %s%n", password);
            if (!mirror.getPassword().equals(password)) {
                // TODO: fail2ban
                continue;
            }

            byte action = received[8];
            System.out.println("Action: " + action);
            switch (action) {
                case 0 -> { // Put pixels
                    System.out.println("Put pixels");
                    /* Format:
                    <4bytes: (int)StartAt> | <2bytes: (short)Length> | <1009bytes: Pixels>
                    9                   12 | 13                   14 | 15             1023
                     */
                    int startAt = NumericUtil.bytesToInt(Arrays.copyOfRange(received, 9, 13));
                    short length = NumericUtil.bytesToShort(Arrays.copyOfRange(received, 13,15));
                    byte[] pixels = Arrays.copyOfRange(received, 15, 15 + length);
//                    if (mirror.getScreen().getLength() < startAt + length) {
//                    }
                    System.out.printf("Start At: %d,  Length: %d,  Actual Length: %d%n", startAt, length, pixels.length);

                    mirror.getScreen().fillPixels(startAt, pixels);
                }
                case 1 -> { // Move cursor
                    /* Format
                    <2bytes: (short)x> | <2bytes: (short)y>
                    9               10 | 11              12
                     */
                    short x = NumericUtil.bytesToShort(Arrays.copyOfRange(received, 9, 11));
                    short y = NumericUtil.bytesToShort(Arrays.copyOfRange(received, 11, 13));
                    mirror.getMapDisplay().getDisplay().setCursorLocation(x, y);
                }
            }
        }

        System.out.println(String.format("Screen mirroring server stopped (%d)", port));
        socket.close();
    }

    public void stopServer() {
        running = false;
    }
}