package com.higgsblock.global.chain.crypto;

import com.google.common.primitives.Bytes;
import org.spongycastle.util.encoders.Hex;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

public class ECKeyTest {

    private static final BigInteger BASE = BigInteger.valueOf(58);

    public static void main(String args[]) {
//        ECKey key = new ECKey();
//        String addr = key.getKeyPair().getAddress();
//        String priKey = key.getKeyPair().getPriKey();
//        String pubKey = key.getKeyPair().getPubKey();
//
//        System.out.println("Addr: " + addr);
//
//        System.out.println("PriKey: " + priKey);
//        System.out.println("PubKey: " + pubKey);
        String addr = "1LZ88bckco6XZRywsLEEgbDtin2wPWGZxV";
        System.out.println("AddrHex: " + Hex.toHexString(decodeBase58To25Bytes(addr)));

        String now = "d67da73f6891af29d6222ab5c0a415b929b98911";
        byte[] nowByte = Hex.decode(now);
        byte[] hash1 = sha256(sha256(Bytes.concat(new byte[]{0}, nowByte)));
        String result = Hex.toHexString(Bytes.concat(new byte[]{0}, nowByte, Arrays.copyOfRange(hash1, 0, 4)));
        System.out.println("AddrHex: " + result);

        System.out.println("AddrHex2: " + encode(Bytes.concat(new byte[]{0}, nowByte, Arrays.copyOfRange(hash1, 0, 4))));
    }

    private static final String ALPHABET = "123456789ABCDEFGHJKLMNPQRSTUVWXYZabcdefghijkmnopqrstuvwxyz";

    private static byte[] decodeBase58To25Bytes(String input) {
        BigInteger num = BigInteger.ZERO;
        for (char t : input.toCharArray()) {
            int p = ALPHABET.indexOf(t);
            if (p == -1)
                return null;
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

    public static String encode(byte[] input) {
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
            if (anInput == 0)
                s.insert(0, ALPHABET.charAt(0));
            else
                break;
        }
        return s.toString();
    }

}