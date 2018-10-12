package com.higgsblock.global.chain.vm.core;

import com.higgsblock.global.chain.vm.DataWord;

import java.util.Map;

/**
 * The interface Contract repository.
 *
 * @author zhao xiaogang
 * @date 2018-09-08
 */
public interface ContractDetails {

    void put(DataWord key, DataWord value);

    DataWord get(DataWord key);

    byte[] getCode();

    void setCode(byte[] code);

    Map<DataWord, DataWord> getStorage();


}
