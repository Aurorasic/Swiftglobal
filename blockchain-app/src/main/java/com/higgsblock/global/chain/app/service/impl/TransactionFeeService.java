package com.higgsblock.global.chain.app.service.impl;

import com.google.common.collect.Lists;
import com.higgsblock.global.chain.app.blockchain.Rewards;
import com.higgsblock.global.chain.app.blockchain.script.LockScript;
import com.higgsblock.global.chain.app.blockchain.transaction.Transaction;
import com.higgsblock.global.chain.app.blockchain.transaction.TransactionOutput;
import com.higgsblock.global.chain.app.service.ITransactionFeeService;
import com.higgsblock.global.chain.app.service.IWitnessService;
import com.higgsblock.global.chain.common.utils.Money;
import com.higgsblock.global.chain.crypto.ECKey;
import com.higgsblock.global.chain.crypto.KeyPair;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @description:
 * @author: yezaiyong
 * @create: 2018-07-21 14:23
 **/
@Slf4j
@Service
public class TransactionFeeService implements ITransactionFeeService {
    public static final Money MINER_REWARDS_RATION = new Money("0.8");

    public static final Money WITNESS_REWARDS_RATION = new Money("0.2");

    public static final Money BLOCK_REWARDS = new Money("3");

    public static final Money BLOCK_REWARDS_MINER = new Money("0.8");

    public static final Money BLOCK_REWARDS_WITNESS = new Money("0.2");

    public static final long WITNESS_NUM = 11;

    @Autowired
    private KeyPair peerKeyPair;

    @Autowired
    private IWitnessService witnessService;

    /**
     * count Miner and Witness Rewards
     *
     * @param totalFee fee transactions
     * @return minerRewards, witnessRewards
     */
    @Override
    public Rewards countMinerAndWitnessRewards(Money totalFee, long height) {

        Money minerTotal;

        LOGGER.debug("miner rewards ration：{} ", MINER_REWARDS_RATION.getValue());

        Money totalMoney = new Money(BLOCK_REWARDS.getValue());
        totalMoney.add(totalFee);

        Rewards rewards = new Rewards();
        rewards.setTotalFee(totalFee);
        rewards.setTotalMoney(totalMoney);
        LOGGER.debug("totalFee:{}", totalFee.getValue());

        //count miner rewards
        minerTotal = new Money(MINER_REWARDS_RATION.getValue()).multiply(totalFee);

        minerTotal.add(new Money(BLOCK_REWARDS_MINER.getValue()).multiply(BLOCK_REWARDS));
        rewards.setMinerTotal(minerTotal);
        LOGGER.debug("Transactions miner rewards total : {}", minerTotal.getValue());

        //count witness rewards
        Money witnessTotal = new Money(BLOCK_REWARDS.getValue()).multiply(BLOCK_REWARDS_WITNESS.getValue());

        witnessTotal.add(new Money(totalFee.getValue()).multiply(WITNESS_REWARDS_RATION));
        Money singleWitnessMoney = new Money(witnessTotal.getValue()).divide(WITNESS_NUM);
        Money lastWitnessMoney = new Money(witnessTotal.getValue()).subtract(new Money(singleWitnessMoney.getValue()).multiply(WITNESS_NUM - 1));

        LOGGER.debug("Transactions witness rewards total : {}, topTenSingleWitnessMoney:{}, lastWitnessMoney:{}", witnessTotal.getValue(),
                singleWitnessMoney.getValue(), lastWitnessMoney.getValue());

        rewards.setTopTenSingleWitnessMoney(singleWitnessMoney);
        rewards.setLastWitnessMoney(lastWitnessMoney);

        if (!rewards.check()) {
            LOGGER.info("count is error block height:{}", height);
            throw new RuntimeException("count money is error");
        }
        return rewards;
    }

    /**
     * @param lockTime lock time
     * @param version  transaction version
     * @param fee      fee map
     * @return return coin base transaction
     */
    @Override
    public Transaction buildCoinBaseTx(long lockTime, short version, Money fee, long height) {
        LOGGER.debug("begin to build coinBase transaction");

        Rewards rewards = countMinerAndWitnessRewards(fee, height);
        Transaction transaction = new Transaction();
        transaction.setVersion(version);
        transaction.setLockTime(lockTime);

        List<TransactionOutput> outputList = Lists.newArrayList();

        String producerAddress = ECKey.pubKey2Base58Address(peerKeyPair);
        TransactionOutput minerCoinBaseOutput = generateTransactionOutput(producerAddress, rewards.getMinerTotal());
        if (minerCoinBaseOutput != null) {
            outputList.add(minerCoinBaseOutput);
        }

        List<TransactionOutput> witnessCoinBaseOutput = genWitnessCoinBaseOutput(rewards, height);
        outputList.addAll(witnessCoinBaseOutput);

        if (CollectionUtils.isEmpty(outputList)) {
            return null;
        }
        transaction.setOutputs(outputList);

        return transaction;
    }


    private TransactionOutput generateTransactionOutput(String address, Money money) {
        if (!ECKey.checkBase58Addr(address)) {
            return null;
        }
        if (!money.checkRange()) {
            return null;
        }
        TransactionOutput output = new TransactionOutput();
        output.setMoney(money);
        LockScript lockScript = new LockScript();
        lockScript.setAddress(address);
        output.setLockScript(lockScript);
        return output;
    }

    private List<TransactionOutput> genWitnessCoinBaseOutput(Rewards rewards, Long height) {
        List<TransactionOutput> outputList = Lists.newArrayList();
        int witnessSize = witnessService.getWitnessSize();
        long lastReward = height % WITNESS_NUM;
        for (int i = 0; i < witnessSize; i++) {
            if (lastReward == i) {
                TransactionOutput transactionOutput = generateTransactionOutput(WitnessService.WITNESS_ADDRESS_LIST.get(i), rewards.getLastWitnessMoney());
                outputList.add(transactionOutput);
            } else {
                TransactionOutput transactionOutput = generateTransactionOutput(WitnessService.WITNESS_ADDRESS_LIST.get(i), rewards.getTopTenSingleWitnessMoney());
                outputList.add(transactionOutput);
            }
        }

        return outputList;
    }

    /**
     * check count money
     *
     * @param coinBaseTransaction coin base transaction
     * @return if count money == totalRewardsMoney result true else result false
     */
    public boolean checkCoinBaseMoney(Transaction coinBaseTransaction, Money totalRewardsMoney) {
        final Money countMoney = new Money();
        coinBaseTransaction.getOutputs().forEach(item -> {
            countMoney.add(item.getMoney());
        });
        return countMoney.compareTo(totalRewardsMoney) == 0;
    }

    /**
     * sort by transaction gasPrice
     *
     * @param transactions wait sort
     * @return sorted transaction by gasPrice
     */
    @Override
    public void sortByGasPrice(List<Transaction> transactions) {
        transactions.sort((tx1, tx2) -> tx2.getGasPrice().compareTo(tx1.getGasPrice()));
    }
}