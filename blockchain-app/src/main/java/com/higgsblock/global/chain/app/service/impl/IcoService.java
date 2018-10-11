package com.higgsblock.global.chain.app.service.impl;

import com.higgsblock.global.chain.app.service.IIcoService;
import com.higgsblock.global.chain.common.enums.SystemCurrencyEnum;
import org.springframework.stereotype.Service;

import java.util.HashSet;

/**
 * @author Chen Jiawei
 * @date 2018-10-11
 */
@Service
public class IcoService implements IIcoService {
    @Override
    public HashSet<String> getContractCurrencies() {
        HashSet<String> contractCurrencies = new HashSet<>();

        contractCurrencies.add(SystemCurrencyEnum.CAS.getCurrency());
        //TODO: chenjiawei get ico currency from db.

        return contractCurrencies;
    }
}
