package com.higgsblock.global.chain.app.vm;

import com.higgsblock.global.chain.app.blockchain.transaction.Transaction;

/**
 * @author tangkun
 * @date 2018-09-11
 */
public class InternalTransaction extends Transaction {

    private byte[] parentHash;

    public byte[] getParentHash() {
        return parentHash;
    }

    public void setParentHash(byte[] parentHash) {
        this.parentHash = parentHash;
    }
}
