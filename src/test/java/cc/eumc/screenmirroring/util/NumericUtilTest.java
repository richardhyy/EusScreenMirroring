package cc.eumc.screenmirroring.util;

import junit.framework.TestCase;

public class NumericUtilTest extends TestCase {
    public void testBytesToShort() {

    }

    public void testShortToBytes() {
        for (short x = Short.MIN_VALUE; x <= Short.MAX_VALUE; x++) {
            byte[] result = NumericUtil.shortToBytes(x);
            assertEquals(x, NumericUtil.bytesToShort(result));
        }
    }


    public void testIntToBytes() {
        for (int x = Short.MIN_VALUE; x <= Short.MAX_VALUE; x++) {
            byte[] result = NumericUtil.intToBytes(x);
            assertEquals(x, NumericUtil.bytesToInt(result));
        }
    }

    public void testBytesToInt() {

    }
}