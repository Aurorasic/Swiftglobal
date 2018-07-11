package com.higgsblock.global.chain.crypto.utils;

/**
 * Base58 interface detection helper class
 *
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
