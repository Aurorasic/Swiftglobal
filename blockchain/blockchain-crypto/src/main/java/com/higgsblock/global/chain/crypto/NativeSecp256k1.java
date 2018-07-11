package com.higgsblock.global.chain.crypto;

import com.google.common.base.Preconditions;

import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import static com.higgsblock.global.chain.crypto.NativeSecp256k1Util.assertEquals;


/**
 * Local elliptic curve dynamic library call
 *
 * @author kongyu
 * @create 2018-02-24 11:24
 */
public class NativeSecp256k1 {
    private static final int TOTAL_CAPACITY = 520;
    private static final int CAPACITY = 32;
    private static final ReentrantReadWriteLock RWL = new ReentrantReadWriteLock();
    private static final Lock R_LOCK = RWL.readLock();
    private static ThreadLocal<ByteBuffer> nativeECDSABuffer = new ThreadLocal<>();

    public static boolean verify(byte[] data, byte[] signature, byte[] pub) throws NativeSecp256k1Util.AssertFailException {
        Preconditions.checkArgument(data.length == 32 && signature.length <= 520 && pub.length <= 520);

        ByteBuffer byteBuff = nativeECDSABuffer.get();
        if (byteBuff == null || byteBuff.capacity() < TOTAL_CAPACITY) {
            byteBuff = ByteBuffer.allocateDirect(TOTAL_CAPACITY);
            byteBuff.order(ByteOrder.nativeOrder());
            nativeECDSABuffer.set(byteBuff);
        }
        byteBuff.rewind();
        byteBuff.put(data);
        byteBuff.put(signature);
        byteBuff.put(pub);

        R_LOCK.lock();
        try {
            return secp256k1_ecdsa_verify(byteBuff, Secp256k1Context.getContext(), signature.length, pub.length) == 1;
        } finally {
            R_LOCK.unlock();
            nativeECDSABuffer.remove();
        }
    }

    public static byte[] sign(byte[] data, byte[] sec) throws NativeSecp256k1Util.AssertFailException {
        Preconditions.checkArgument(data.length == 32 && sec.length <= 32);

        ByteBuffer byteBuff = nativeECDSABuffer.get();
        if (byteBuff == null || byteBuff.capacity() < CAPACITY + CAPACITY) {
            byteBuff = ByteBuffer.allocateDirect(CAPACITY + CAPACITY);
            byteBuff.order(ByteOrder.nativeOrder());
            nativeECDSABuffer.set(byteBuff);
        }
        byteBuff.rewind();
        byteBuff.put(data);
        byteBuff.put(sec);

        byte[][] retByteArray;

        R_LOCK.lock();
        try {
            retByteArray = secp256k1_ecdsa_sign(byteBuff, Secp256k1Context.getContext());
        } finally {
            R_LOCK.unlock();
        }

        byte[] sigArr = retByteArray[0];
        int sigLen = new BigInteger(new byte[]{retByteArray[1][0]}).intValue();
        int retVal = new BigInteger(new byte[]{retByteArray[1][1]}).intValue();

        assertEquals(sigArr.length, sigLen, "Got bad signature length.");

        return retVal == 0 ? new byte[0] : sigArr;
    }

    private static native long secp256k1_ctx_clone(long context);

    private static native int secp256k1_context_randomize(ByteBuffer byteBuff, long context);

    private static native byte[][] secp256k1_privkey_tweak_add(ByteBuffer byteBuff, long context);

    private static native byte[][] secp256k1_privkey_tweak_mul(ByteBuffer byteBuff, long context);

    private static native byte[][] secp256k1_pubkey_tweak_add(ByteBuffer byteBuff, long context, int pubLen);

    private static native byte[][] secp256k1_pubkey_tweak_mul(ByteBuffer byteBuff, long context, int pubLen);

    private static native void secp256k1_destroy_context(long context);

    private static native int secp256k1_ecdsa_verify(ByteBuffer byteBuff, long context, int sigLen, int pubLen);

    private static native byte[][] secp256k1_ecdsa_sign(ByteBuffer byteBuff, long context);

    private static native int secp256k1_ec_seckey_verify(ByteBuffer byteBuff, long context);

    private static native byte[][] secp256k1_ec_pubkey_create(ByteBuffer byteBuff, long context);

    private static native byte[][] secp256k1_ec_pubkey_parse(ByteBuffer byteBuff, long context, int inputLen);

    private static native byte[][] secp256k1_schnorr_sign(ByteBuffer byteBuff, long context);

    private static native byte[][] secp256k1_ecdh(ByteBuffer byteBuff, long context, int inputLen);
}
