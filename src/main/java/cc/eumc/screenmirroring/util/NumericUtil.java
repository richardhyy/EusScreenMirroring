package cc.eumc.screenmirroring.util;

import java.nio.ByteBuffer;

public class NumericUtil {
    public static byte[] shortToBytes(short x) {
        return ByteBuffer.allocate(2).putShort(x).array();
//        byte[] result = new byte[2];
//        result[0] = (byte) (x >> 8);
//        result[1] = (byte) (x & 0xff);
//        return result;
    }

    public static short bytesToShort(byte[] twoBytes) {
        return ByteBuffer.wrap(twoBytes).getShort();
//        return (short) ((high << 8) | low);
    }

    public static byte[] intToBytes(int x) {
        return ByteBuffer.allocate(4).putInt(x).array();
    }

    public static int bytesToInt(byte[] fourBytes) {
        return ByteBuffer.wrap(fourBytes).getInt();
    }
}
