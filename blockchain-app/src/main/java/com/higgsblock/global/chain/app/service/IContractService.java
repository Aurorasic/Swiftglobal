package com.higgsblock.global.chain.app.service;

import com.higgsblock.global.chain.app.blockchain.Block;
import com.higgsblock.global.chain.app.blockchain.transaction.Transaction;
import com.higgsblock.global.chain.app.service.impl.ContractService;
import com.higgsblock.global.chain.common.utils.Money;
import com.higgsblock.global.chain.vm.core.Repository;

/**
 * @author tangkun
 * @date 2018-10-10
 */
public interface IContractService {

    ContractService.InvokePO invoke(Block block, Transaction transaction, Repository transactionRepository, int totalUsedSize, long totalUsedGas, Money totalFee);
}
