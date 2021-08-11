package cc.eumc.screenmirroringclient;

import cc.eumc.screenmirroringclient.model.RemoteMirror;
import cc.eumc.screenmirroringclient.model.Screen;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

public class DataSender extends Thread {
    private final UdpClient client;
    private final RemoteMirror mirror;
    private boolean running = true;
    private boolean clearing = false;
    private final Queue<byte[]> pendingData = new ConcurrentLinkedQueue<>();
    private final Map<Integer, byte[]> latestPixels = new HashMap<>();
    private boolean incrementalUpdate;

    public DataSender(UdpClient client, RemoteMirror mirror, boolean incrementalUpdate) {
        this.client = client;
        this.mirror = mirror;
        this.incrementalUpdate = incrementalUpdate;
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
                byte[] lastPixels = latestPixels.get(i);
                byte[] newPixels = Arrays.copyOfRange(pixels, startAt, Math.min(pixels.length, startAt + PacketBuilder.MAX_PIXEL_LENGTH));
                if (lastPixels == null || (!incrementalUpdate || !Arrays.equals(lastPixels, newPixels))) {
                    // Only send put pixel request when screen content changes
                    queueDataPacket(PacketBuilder.createPutPixelPacket(mirror.getId(), mirror.getPassword(), startAt, newPixels));
                    latestPixels.put(i, newPixels);
                }
            }
        }
    }

    public void sendMouseCoordinates(short x, short y) {
        queueDataPacket(PacketBuilder.createMoveCursorPacket(mirror.getId(), mirror.getPassword(), x, y));
    }

    public void sendShowDisconnectScreen() {
        queueDataPacket(PacketBuilder.createShowDisconnectScreenPacket(mirror.getId(), mirror.getPassword()));
    }

    private void queueDataPacket(byte[] data) {
        pendingData.add(data);
    }

    public void clearPending() {
        clearing = true;
        pendingData.clear();
        latestPixels.clear();
    }

    public void stopSending() {
        running = false;
    }
}
