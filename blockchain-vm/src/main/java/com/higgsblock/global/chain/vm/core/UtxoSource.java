package com.higgsblock.global.chain.vm.core;

/**
 * @author tangkun
 * @date 2018-09-10
 */
public class UtxoSource implements Source <String,UTXOBO> {



    @Override
    public void put(String key, UTXOBO val) {

    }

    @Override
    public UTXOBO get(String key) {
        return null;
    }

    @Override
    public void delete(String key) {

    }

    @Override
    public boolean flush() {
        return false;
    }
}
