package cn.primeledger.cas.global.utils;

/**
 * @author kongyu
 * @date 2018-02-24 11:09
 */
public class Sha256CheckSumProvider implements IBase58CheckSumProvider {
    @Override
    public byte[] calculateActualCheckSum(byte[] data) {
        return Sha256Hash.hashTwice(data);
    }
}
