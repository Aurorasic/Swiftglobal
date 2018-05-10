package com.higgsblock.global.chain.crypto.utils;

import com.google.common.base.Charsets;
import com.google.common.io.BaseEncoding;
import org.spongycastle.crypto.digests.RIPEMD160Digest;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.nio.charset.Charset;
import java.util.Date;

import static com.google.common.base.Preconditions.checkArgument;

/**
 * @author kongyu
 * @create 2018-02-23 16:24
 */
public class CryptoUtils {
    public static final String CASGLOBAL_SIGNED_HEADER = "CAS.global Signed Message:\n";
    public static final byte[] CASGLOBAL_SIGNED_HEADER_BYTES = CASGLOBAL_SIGNED_HEADER.getBytes(Charsets.UTF_8);

    public static final BaseEncoding HEX = BaseEncoding.base16().lowerCase();

    public static volatile Date mockTime;

    private static int isAndroid = -1;

    private CryptoUtils() {
    }

    public static boolean isAndroidRuntime() {
        if (isAndroid == -1) {
            final String runtime = System.getProperty("java.runtime.name");
            isAndroid = (runtime != null && "Android Runtime".equals(runtime)) ? 1 : 0;
        }
        return isAndroid == 1;
    }

    public static long currentTimeSeconds() {
        return currentTimeMillis() / 1000;
    }

    public static long currentTimeMillis() {
        return mockTime != null ? mockTime.getTime() : System.currentTimeMillis();
    }

    public static byte[] bigIntegerToBytes(BigInteger b, int numBytes) {
        checkArgument(b.signum() >= 0, "b must be positive or zero");
        checkArgument(numBytes > 0, "numBytes must be positive");
        byte[] src = b.toByteArray();
        byte[] dest = new byte[numBytes];
        boolean isFirstByteOnlyForSign = src[0] == 0;
        int length = isFirstByteOnlyForSign ? src.length - 1 : src.length;
        checkArgument(length <= numBytes, "The given number does not fit in " + numBytes);
        int srcPos = isFirstByteOnlyForSign ? 1 : 0;
        int destPos = numBytes - length;
        System.arraycopy(src, srcPos, dest, destPos, length);
        return dest;
    }

    public static byte[] reverseBytes(byte[] bytes) {
        byte[] buf = new byte[bytes.length];
        for (int i = 0; i < bytes.length; i++) {
            buf[i] = bytes[bytes.length - 1 - i];
        }
        return buf;
    }

    public static byte[] sha256hash160(byte[] input) {
        byte[] sha256 = Sha256Hash.hash(input);
        RIPEMD160Digest digest = new RIPEMD160Digest();
        digest.update(sha256, 0, sha256.length);
        byte[] out = new byte[20];
        digest.doFinal(out, 0);
        return out;
    }

    public static byte[] formatMessageForSigning(String message, Charset charset, byte[] headerBytes) {
        try {
            ByteArrayOutputStream bos = new ByteArrayOutputStream();

            bos.write(CASGLOBAL_SIGNED_HEADER_BYTES.length);
            bos.write(CASGLOBAL_SIGNED_HEADER_BYTES);

            if (headerBytes != null && headerBytes.length > 0) {
                bos.write(headerBytes.length);
                bos.write(headerBytes);
            }

            byte[] messageBytes = message.getBytes(charset);
            VarLength size = new VarLength(messageBytes.length);
            bos.write(size.encode());
            bos.write(messageBytes);
            return bos.toByteArray();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static long readUint32(byte[] bytes, int offset) {
        return (bytes[offset] & 0xffL) | ((bytes[offset + 1] & 0xffL) << 8) | ((bytes[offset + 2] & 0xffL) << 16)
                | ((bytes[offset + 3] & 0xffL) << 24);
    }

    public static long readInt64(byte[] bytes, int offset) {
        return (bytes[offset] & 0xffL) | ((bytes[offset + 1] & 0xffL) << 8) | ((bytes[offset + 2] & 0xffL) << 16)
                | ((bytes[offset + 3] & 0xffL) << 24) | ((bytes[offset + 4] & 0xffL) << 32)
                | ((bytes[offset + 5] & 0xffL) << 40) | ((bytes[offset + 6] & 0xffL) << 48)
                | ((bytes[offset + 7] & 0xffL) << 56);
    }

    public static void uint32ToByteArrayBE(long val, byte[] out, int offset) {
        out[offset] = (byte) (0xFF & (val >> 24));
        out[offset + 1] = (byte) (0xFF & (val >> 16));
        out[offset + 2] = (byte) (0xFF & (val >> 8));
        out[offset + 3] = (byte) (0xFF & val);
    }

    public static void uint32ToByteArrayLE(long val, byte[] out, int offset) {
        out[offset] = (byte) (0xFF & val);
        out[offset + 1] = (byte) (0xFF & (val >> 8));
        out[offset + 2] = (byte) (0xFF & (val >> 16));
        out[offset + 3] = (byte) (0xFF & (val >> 24));
    }

    public static void uint64ToByteArrayLE(long val, byte[] out, int offset) {
        out[offset] = (byte) (0xFF & val);
        out[offset + 1] = (byte) (0xFF & (val >> 8));
        out[offset + 2] = (byte) (0xFF & (val >> 16));
        out[offset + 3] = (byte) (0xFF & (val >> 24));
        out[offset + 4] = (byte) (0xFF & (val >> 32));
        out[offset + 5] = (byte) (0xFF & (val >> 40));
        out[offset + 6] = (byte) (0xFF & (val >> 48));
        out[offset + 7] = (byte) (0xFF & (val >> 56));
    }
}
