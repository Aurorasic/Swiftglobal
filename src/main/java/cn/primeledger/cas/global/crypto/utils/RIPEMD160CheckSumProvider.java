package cn.primeledger.cas.global.crypto.utils;

import org.spongycastle.crypto.digests.RIPEMD160Digest;

/**
 * @author kongyu
 * @date 2018-02-24 11:23
 */
public class RIPEMD160CheckSumProvider implements IBase58CheckSumProvider {
    @Override
    public byte[] calculateActualCheckSum(byte[] data) {
        RIPEMD160Digest ripemd160Digest = new RIPEMD160Digest();
        ripemd160Digest.update(data, 0, data.length);
        byte[] actualChecksum = new byte[ripemd160Digest.getDigestSize()];
        ripemd160Digest.doFinal(actualChecksum, 0);
        return actualChecksum;
    }
}
