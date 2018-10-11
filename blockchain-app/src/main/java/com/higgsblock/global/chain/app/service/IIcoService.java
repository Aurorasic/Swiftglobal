package com.higgsblock.global.chain.app.service;

import java.util.HashSet;

/**
 * @author Chen Jiawei
 * @date 2018-10-11
 */
public interface IIcoService {
    /**
     * Gets currencies that can be transferred to contract.
     *
     * @return collection of currencies.
     */
    HashSet<String> getContractCurrencies();
}
