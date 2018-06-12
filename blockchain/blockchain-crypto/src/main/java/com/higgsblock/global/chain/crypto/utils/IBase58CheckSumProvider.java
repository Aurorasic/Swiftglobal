package com.higgsblock.global.chain.crypto.utils;

/**
 * @author kongyu
 * @date 2018-02-24 11:09
 */
public interface IBase58CheckSumProvider {
    /**
     * calculateActualCheckSum
     *
     * @param data
     * @return
     */
    public byte[] calculateActualCheckSum(byte[] data);
}
