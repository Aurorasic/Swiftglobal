package com.higgsblock.global.chain.app.utils;

import com.google.common.primitives.Bytes;
import org.springframework.util.StringUtils;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

public class AddrUtil {

    private static final String ALPHABET = "123456789ABCDEFGHJKLMNPQRSTUVWXYZabcdefghijkmnopqrstuvwxyz";
    private static final BigInteger BASE = BigInteger.valueOf(58);

    public static byte[] toContractAddr(String txAddr) {
        if (StringUtils.isEmpty(txAddr)) {
            throw new IllegalArgumentException("Can not be null");
        }

        byte[] decoded = decodeBase58To25Bytes(txAddr);

        return Arrays.copyOfRange(decoded, 1, 21);
    }


    public static String toTransactionAddr(byte[] contractAddr) {
        byte[] hash1 = sha256(sha256(Bytes.concat(new byte[]{0}, contractAddr)));

        return encode(Bytes.concat(new byte[]{0}, contractAddr, Arrays.copyOfRange(hash1, 0, 4)));
    }


    private static byte[] decodeBase58To25Bytes(String input) {
        BigInteger num = BigInteger.ZERO;
        for (char t : input.toCharArray()) {
            int p = ALPHABET.indexOf(t);
            if (p == -1) {
                return null;
            }
            num = num.multiply(BigInteger.valueOf(58)).add(BigInteger.valueOf(p));
        }

        byte[] result = new byte[25];
        byte[] numBytes = num.toByteArray();
        System.arraycopy(numBytes, 0, result, result.length - numBytes.length, numBytes.length);
        return result;
    }

    private static byte[] sha256(byte[] data) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            md.update(data);
            return md.digest();
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }

    private static String encode(byte[] input) {
        // This could be a lot more efficient.
        BigInteger bi = new BigInteger(1, input);
        StringBuffer s = new StringBuffer();
        while (bi.compareTo(BASE) >= 0) {
            BigInteger mod = bi.mod(BASE);
            s.insert(0, ALPHABET.charAt(mod.intValue()));
            bi = bi.subtract(mod).divide(BASE);
        }
        s.insert(0, ALPHABET.charAt(bi.intValue()));
        // Convert leading zeros too.
        for (byte anInput : input) {
            if (anInput == 0) {
                s.insert(0, ALPHABET.charAt(0));
            } else {
                break;
            }
        }
        return s.toString();
    }

    public static void main(String args[]) {
        byte[] contractAddr = toContractAddr("1LZ88bckco6XZRywsLEEgbDtin2wPWGZxV");

        System.out.println(toTransactionAddr(contractAddr));
    }

}
