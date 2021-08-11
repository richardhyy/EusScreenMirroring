package cc.eumc.screenmirroringclient.playback;

import cc.eumc.screenmirroringclient.util.NumericUtil;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class PacketRecorder {
    File recordFile;
    BufferedOutputStream bufferedOutputStream;
    boolean closed;

    public PacketRecorder(File destination) throws IOException {
        this.recordFile = destination;
        this.bufferedOutputStream = new BufferedOutputStream(new FileOutputStream(destination));
        this.closed = false;
    }

    public static PacketRecorder createPacketRecorder(File destinationFolder) throws IOException {
        if (!destinationFolder.exists()) {
            destinationFolder.mkdirs();
        }
        File outputFile = new File(destinationFolder, new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date(System.currentTimeMillis())) + ".rec");
        return new PacketRecorder(outputFile);
    }

    public void write(byte[] rawPacket) {
        if (closed) {
            System.err.println("Already closed");
        }

        try {
            bufferedOutputStream.write(createFrameBytes(rawPacket));
        } catch (IOException e) {
            System.err.println(e.getMessage());
        }
    }

    public void close() throws IOException {
        closed = true;
        bufferedOutputStream.close();
    }

    private byte[] createFrameBytes(byte[] rawPacket) {
        /*
        Format:
        <long: millisecondsSince1970> | <short: length> | <data>
        0                           7 | 8            9 | 10 ...
         */
        byte[] frame = new byte[rawPacket.length + 2]; // 2 = 10 - 8 = frame meta length - packet.(id & password).length
        System.arraycopy(NumericUtil.longToBytes(System.currentTimeMillis()), 0, frame, 0, 8);
        System.arraycopy(NumericUtil.shortToBytes((short) (rawPacket.length - 8)), 0, frame, 8, 2);
        System.arraycopy(rawPacket, 8, frame, 10, rawPacket.length - 8); // start copy from the `action` byte
        return frame;
    }

    public File getRecordFile() {
        return recordFile;
    }
}
