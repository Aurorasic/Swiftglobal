package cn.primeledger.cas.global.p2p.utils;

import com.google.common.primitives.UnsignedLong;

import java.math.BigInteger;
import java.util.concurrent.ThreadLocalRandom;

public class Rnd {

    private static final BigInteger TWO = BigInteger.valueOf(2);

    public static long rndLong() {
        return ThreadLocalRandom.current().nextLong();
    }

    public static long rndLong(long min, long max) {
        return ThreadLocalRandom.current().nextLong(min, max);
    }

    public static UnsignedLong rndUnsignedLong() {
        byte[] b = new byte[8];
        ThreadLocalRandom.current().nextBytes(b);
        return UnsignedLong.valueOf(new BigInteger(b).abs().multiply(TWO));
    }

}
