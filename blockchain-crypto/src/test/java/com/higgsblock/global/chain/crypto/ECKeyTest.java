package com.higgsblock.global.chain.crypto;

import org.spongycastle.util.encoders.Hex;

import java.math.BigInteger;

public class ECKeyTest {

    public static void main(String args[]) {
        ECKey key = new ECKey();
        String addr = key.getKeyPair().getAddress();
        String priKey = key.getKeyPair().getPriKey();
        String pubKey = key.getKeyPair().getPubKey();

        System.out.println("Addr: " + addr);

        System.out.println("PriKey: " + priKey);
        System.out.println("PubKey: " + pubKey);
        System.out.println("AddrHex: " + Hex.toHexString(decodeBase58To25Bytes(addr)));
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
}
