package com.higgsblock.global.chain.crypto.utils;

import com.google.common.io.ByteStreams;
import com.google.common.primitives.Ints;
import lombok.Getter;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.Serializable;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

import static com.google.common.base.Preconditions.checkArgument;

/**
 * Hash256 tool class
 *
 * @author kongyu
 * @date 2018-02-23 15:42
 */
@Getter
public class Sha256Hash implements Serializable, Comparable<Sha256Hash> {
    public static final int LENGTH = 32;

    public static final Sha256Hash ZERO_HASH = wrap(new byte[LENGTH]);

    private final byte[] bytes;

    private Sha256Hash(byte[] rawHashBytes) {
        checkArgument(rawHashBytes.length == LENGTH);
        this.bytes = rawHashBytes;
    }

    public static Sha256Hash wrap(String hexString) {
        return wrap(CryptoUtils.HEX.decode(hexString));
    }

    public static Sha256Hash wrapReversed(byte[] rawHashBytes) {
        return wrap(CryptoUtils.reverseBytes(rawHashBytes));
    }

    public static Sha256Hash of(byte[] contents) {
        return wrap(hash(contents));
    }

    public static Sha256Hash twiceOf(byte[] contents) {
        return wrap(hashTwice(contents));
    }

    public static Sha256Hash of(File file) throws IOException {
        FileInputStream in = new FileInputStream(file);
        try {
            return of(ByteStreams.toByteArray(in));
        } finally {
            in.close();
        }
    }

    public static MessageDigest newDigest() {
        try {
            return MessageDigest.getInstance("SHA-256");
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }

    public static byte[] hash(byte[] input) {
        return hash(input, 0, input.length);
    }

    public static byte[] hash(byte[] input, int offset, int length) {
        MessageDigest digest = newDigest();
        digest.update(input, offset, length);
        return digest.digest();
    }

    public static byte[] hashTwice(byte[] input) {
        return hashTwice(input, 0, input.length);
    }

    public static byte[] hashTwice(byte[] input, int offset, int length) {
        MessageDigest digest = newDigest();
        digest.update(input, offset, length);
        return digest.digest(digest.digest());
    }

    public static byte[] hashTwice(byte[] input1, int offset1, int length1, byte[] input2, int offset2, int length2) {
        MessageDigest digest = newDigest();
        digest.update(input1, offset1, length1);
        digest.update(input2, offset2, length2);
        return digest.digest(digest.digest());
    }

    private static Sha256Hash wrap(byte[] rawHashBytes) {
        return new Sha256Hash(rawHashBytes);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        return Arrays.equals(bytes, ((Sha256Hash) o).bytes);
    }

    @Override
    public int hashCode() {
        return Ints.fromBytes(bytes[LENGTH - 4], bytes[LENGTH - 3], bytes[LENGTH - 2], bytes[LENGTH - 1]);
    }

    @Override
    public String toString() {
        return CryptoUtils.HEX.encode(bytes);
    }

    public BigInteger toBigInteger() {
        return new BigInteger(1, bytes);
    }

    public byte[] getReversedBytes() {
        return CryptoUtils.reverseBytes(bytes);
    }

    @Override
    public int compareTo(final Sha256Hash other) {
        for (int i = LENGTH - 1; i >= 0; i--) {
            final int thisByte = this.bytes[i] & 0xff;
            final int otherByte = other.bytes[i] & 0xff;
            if (thisByte > otherByte) {
                return 1;
            }
            if (thisByte < otherByte) {
                return -1;
            }
        }
        return 0;
    }
}
