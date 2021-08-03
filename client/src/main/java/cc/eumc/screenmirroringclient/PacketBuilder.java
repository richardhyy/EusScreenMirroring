package cc.eumc.screenmirroringclient;

import cc.eumc.screenmirroringclient.util.NumericUtil;

import java.nio.charset.StandardCharsets;

public class PacketBuilder {
    public static final byte PUT_PIXELS = 0;
    public static final byte MOVE_CURSOR = 1;
    public static final byte SHOW_DISCONNECT_SCREEN = 2;

    public static final int MAX_PIXEL_LENGTH = 1009;

    /**
     * Format:
     * <2bytes: ID> | <6bytes: Password> | <1byte: Action> | <1015bytes: Data>
     * 0          1 | 2                7 | 8             8 | 9            1023
     * @param mirrorID
     * @param password
     * @param action
     * @return
     */
    public static byte[] createBasePacket(short mirrorID, String password, byte action) {
        byte[] data = new byte[1024];
        System.arraycopy(NumericUtil.shortToBytes(mirrorID), 0, data, 0, 2);
        System.arraycopy(password.getBytes(StandardCharsets.US_ASCII), 0, data, 2, 6);
        data[8] = action;
        return data;
    }

    /**
     * Format:
     * <4bytes: (int)StartAt> | <2bytes: (short)Length> | <1009bytes: Pixels>
     * 9                   12 | 13                   14 | 15             1023
     * @param mirrorID
     * @param password
     * @param startAt
     * @param pixels should be no more than 1009 pixels
     * @return
     */
    public static byte[] createPutPixelPacket(short mirrorID, String password, int startAt, byte[] pixels) {
        byte[] data = createBasePacket(mirrorID, password, PUT_PIXELS);
        System.arraycopy(NumericUtil.intToBytes(startAt), 0, data, 9, 4);
        System.arraycopy(NumericUtil.shortToBytes((short) pixels.length), 0, data, 13, 2);
        System.arraycopy(pixels, 0, data, 15, pixels.length);
        return data;
    }

    /**
     * Format:
     * <2bytes: (short)x> | <2bytes: (short)y>
     * 9               10 | 11              12
     * @param mirrorID
     * @param password
     * @param x
     * @param y
     * @return
     */
    public static byte[] createMoveCursorPacket(short mirrorID, String password, short x, short y) {
        byte[] data = createBasePacket(mirrorID, password, MOVE_CURSOR);
        System.arraycopy(NumericUtil.shortToBytes(x), 0, data, 9, 2);
        //noinspection SuspiciousNameCombination
        System.arraycopy(NumericUtil.shortToBytes(y), 0, data, 11, 2);
        return data;
    }

    /**
     * Ask the server to show a blank screen with disconnect icon
     * @param mirrorID
     * @param password
     * @return
     */
    public static byte[] createShowDisconnectScreenPacket(short mirrorID, String password) {
        return createBasePacket(mirrorID, password, SHOW_DISCONNECT_SCREEN);
    }
}
