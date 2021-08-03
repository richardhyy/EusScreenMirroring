package cc.eumc.screenmirroringclient;

import cc.eumc.screenmirroringclient.model.RemoteMirror;
import cc.eumc.screenmirroringclient.model.Screen;

import java.io.IOException;
import java.util.Arrays;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

public class DataSender extends Thread {
    private final UdpClient client;
    private final RemoteMirror mirror;
    private boolean running = true;
    private boolean clearing = false;
    private final Queue<byte[]> pendingData = new ConcurrentLinkedQueue<>();

    public DataSender(UdpClient client, RemoteMirror mirror) {
        this.client = client;
        this.mirror = mirror;
    }

    @Override
    public void run() {
        while (running) {
            if (pendingData.peek() != null) {
                byte[] data = pendingData.poll();
                try {
                    client.send(data);

//                    System.out.print(".");

                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        client.close();
    }

    public void sendScreen(Screen screen) {
        byte[] pixels = screen.getFlattenedPixels();
        int packetNumber = pixels.length / PacketBuilder.MAX_PIXEL_LENGTH + (pixels.length % PacketBuilder.MAX_PIXEL_LENGTH == 0 ? 0 : 1);
        for (int i = 0; i < packetNumber; i++) {
            int startAt = i * PacketBuilder.MAX_PIXEL_LENGTH;
            if (clearing) {
                clearing = false;
                break;
            } else {
                pendingData.add(PacketBuilder.createPutPixelPacket(mirror.getId(), mirror.getPassword(), startAt, Arrays.copyOfRange(pixels, startAt, Math.min(pixels.length, startAt + PacketBuilder.MAX_PIXEL_LENGTH))));
            }
        }
    }

    public void sendMouseCoordinates(short x, short y) {
        pendingData.add(PacketBuilder.createMoveCursorPacket(mirror.getId(), mirror.getPassword(), x, y));
    }

    public void sendShowDisconnectScreen() {
        pendingData.add(PacketBuilder.createShowDisconnectScreenPacket(mirror.getId(), mirror.getPassword()));
    }

    public void clearPending() {
        clearing = true;
        pendingData.clear();
    }

    public void stopSending() {
        running = false;
    }
}
