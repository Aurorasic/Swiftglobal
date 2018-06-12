package com.higgsblock.global.browser.service.impl;

import com.higgsblock.global.browser.service.iface.*;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @author kongyu
 * @date 2018-5-25 21:11
 */
@Component
@Data
public class OperatorServiceFactory {
    @Autowired
    private IBlockHeaderService iBlockHeaderService;

    @Autowired
    private IBlockService iBlockService;

    @Autowired
    private IMinersService iMinersService;

    @Autowired
    private IRewardService iRewardService;

    @Autowired
    private ITransactionOutputService iTransactionOutputService;

    @Autowired
    private ITransactionService iTransactionService;

    @Autowired
    private ITransactionInputService iTransactionInputService;

    @Autowired
    private IUTXOService iutxoService;

}
