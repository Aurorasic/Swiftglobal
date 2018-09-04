package com.higgsblock.global.chain.app.keyvalue.core;

/**
 * @author baizhengwen
 * @date 2018-08-27
 */
public interface ITransactionAwareKeyValueAdapter extends IndexedKeyValueAdapter {

    void beginTransaction();

    void rollbackTransaction();

    void commitTransaction();

}
