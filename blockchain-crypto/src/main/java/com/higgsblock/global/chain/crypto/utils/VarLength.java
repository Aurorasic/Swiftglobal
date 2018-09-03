package com.higgsblock.global.chain.crypto.utils;

/**
 * Elliptic curve encryption algorithm calculation auxiliary class
 *
 * @author kongyu
 * @create 2018-02-23 15:42
 */
public class VarLength {
    private static final long FIRST_VAR_LENGTH_ONE = 253L;
    private static final long FIRST_VAR_LENGTH_TWO = 254L;
    private static final long MIDDLE_SIZE = 0xFFFFL;
    private static final long MAX_SIZE = 0xFFFFL;
    public final long value;
    private final int originallyEncodedSize;

    public VarLength(long value) {
        this.value = value;
        originallyEncodedSize = getSizeInBytes();
    }

    public VarLength(byte[] buf, int offset) {
        int first = 0xFF & buf[offset];
        if (first < FIRST_VAR_LENGTH_ONE) {
            value = first;
            // 1 request byte (8 bits)
            originallyEncodedSize = 1;
        } else if (first == FIRST_VAR_LENGTH_ONE) {
            value = (0xFF & buf[offset + 1]) | ((0xFF & buf[offset + 2]) << 8);
            // 1 marker + 2 request bytes (16 bits)
            originallyEncodedSize = 3;
        } else if (first == FIRST_VAR_LENGTH_TWO) {
            value = CryptoUtils.readUint32(buf, offset + 1);
            // 1 marker + 4 request bytes (32 bits)
            originallyEncodedSize = 5;
        } else {
            value = CryptoUtils.readInt64(buf, offset + 1);
            // 1 marker + 8 request bytes (64 bits)
            originallyEncodedSize = 9;
        }
    }

    public static int sizeOf(long value) {
        // if negative, it's actually a very large unsigned long value
        if (value < 0) {
            // 1 marker + 8 request bytes
            return 9;
        }
        if (value < FIRST_VAR_LENGTH_ONE) {
            // 1 request byte
            return 1;
        }
        if (value <= MIDDLE_SIZE) {
            // 1 marker + 2 request bytes
            return 3;
        }
        if (value <= MAX_SIZE) {
            // 1 marker + 4 request bytes
            return 5;
        }
        // 1 marker + 8 request bytes
        return 9;
    }

    public byte[] encode() {
        byte[] bytes;
        switch (sizeOf(value)) {
            case 1:
                return new byte[]{(byte) value};
            case 3:
                return new byte[]{(byte) 253, (byte) (value), (byte) (value >> 8)};
            case 5:
                bytes = new byte[5];
                bytes[0] = (byte) 254;
                CryptoUtils.uint32ToByteArrayLE(value, bytes, 1);
                return bytes;
            default:
                bytes = new byte[9];
                bytes[0] = (byte) 255;
                CryptoUtils.uint64ToByteArrayLE(value, bytes, 1);
                return bytes;
        }
    }

    public final int getSizeInBytes() {
        return sizeOf(value);
    }
}
