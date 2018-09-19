package com.higgsblock.global.chain.vm.core;

import com.higgsblock.global.chain.vm.DataWord;

import java.util.Map;

public interface ContractDetails {

    Map<DataWord, DataWord> getStorage();
}
