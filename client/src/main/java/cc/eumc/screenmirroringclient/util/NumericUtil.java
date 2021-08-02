package cc.eumc.screenmirroringclient.util;

import java.nio.ByteBuffer;

public class NumericUtil {
    public static byte[] shortToBytes(short x) {
        return ByteBuffer.allocate(2).putShort(x).array();
    }

    public static short bytesToShort(byte[] twoBytes) {
        return ByteBuffer.wrap(twoBytes).getShort();
    }

    public static byte[] intToBytes(int x) {
        return ByteBuffer.allocate(4).putInt(x).array();
    }

    public static int bytesToInt(byte[] fourBytes) {
        return ByteBuffer.wrap(fourBytes).getInt();
    }
}
