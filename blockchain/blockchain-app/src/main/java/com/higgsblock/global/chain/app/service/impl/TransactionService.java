package com.higgsblock.global.chain.app.service.impl;

import com.google.common.collect.Lists;
import com.higgsblock.global.chain.app.blockchain.Block;
import com.higgsblock.global.chain.app.blockchain.BlockIndex;
import com.higgsblock.global.chain.app.blockchain.Rewards;
import com.higgsblock.global.chain.app.blockchain.listener.MessageCenter;
import com.higgsblock.global.chain.app.blockchain.script.LockScript;
import com.higgsblock.global.chain.app.blockchain.script.UnLockScript;
import com.higgsblock.global.chain.app.blockchain.transaction.*;
import com.higgsblock.global.chain.app.dao.entity.TransactionIndexEntity;
import com.higgsblock.global.chain.app.dao.entity.UTXOEntity;
import com.higgsblock.global.chain.app.service.ITransactionService;
import com.higgsblock.global.chain.app.service.IWitnessService;
import com.higgsblock.global.chain.common.enums.SystemCurrencyEnum;
import com.higgsblock.global.chain.common.utils.Money;
import com.higgsblock.global.chain.crypto.ECKey;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * @description:
 * @author: yezaiyong
 * @create: 2018-07-21 12:38
 **/
@Service
@Slf4j
public class TransactionService implements ITransactionService {
    /**
     * 11+1
     */
    private static final int MIN_OUTPUT_SIZE = 11 + 1;

    private static final int TRANSACTION_NUMBER = 2;

    @Autowired
    private TransactionCacheManager txCacheManager;

    @Autowired
    private MessageCenter messageCenter;

    @Autowired
    private BlockService blockService;

    @Autowired
    private BestUTXOService bestUtxoService;

    @Autowired
    private UTXOServiceProxy utxoServiceProxy;

    @Autowired
    private TransactionIndexService transactionIndexService;

    @Autowired
    private BlockIndexService blockIndexService;

    @Autowired
    private TransactionFeeService transactionFeeService;

    @Autowired
    private IWitnessService witnessService;

    @Override
    public boolean validTransactions(Block block) {
        LOGGER.info("begin to check the transactions of block {}", block.getHeight());

        //step1 valid block transaction is null
        List<Transaction> transactions = block.getTransactions();
        if (CollectionUtils.isEmpty(transactions)) {
            LOGGER.error("transactions is empty, block_hash={}", block.getHash());
            return false;
        }

        //step2 valid transaction size
        int tx_number = transactions.size();
        if (TRANSACTION_NUMBER > tx_number) {
            LOGGER.error("transactions number is less than two, block_hash={}", block.getHash());
            return false;
        }

        //step3 valid info
        for (int index = 0; index < tx_number; index++) {
            boolean isCoinBaseTx = index == 0 ? true : false;
            //step1 valid tx isCoinBase
            if (isCoinBaseTx) {
                if (!transactions.get(index).isEmptyInputs()) {
                    LOGGER.error("Invalidate Coinbase transaction");
                    return false;
                }
                if (!validCoinBaseTx(transactions.get(index), block)) {
                    LOGGER.error("Invalidate Coinbase transaction");
                    return false;
                }
            }
            //step2 valid tx business info
            if (!verifyTransactionInputAndOutputInfo(transactions.get(index), block)) {
                return false;
            }
        }
        LOGGER.info("check the transactions success of block {}", block.getHeight());
        return true;
    }

    @Override
    public void receivedTransaction(Transaction tx) {
        {
            String hash = tx.getHash();
            LOGGER.info("receive a new transaction from remote with hash {} and data {}", hash, tx);
            Map<String, Transaction> transactionMap = txCacheManager.getTransactionMap().asMap();
            if (transactionMap.containsKey(hash)) {
                LOGGER.info("the transaction is exist in cache with hash {}", hash);
                return;
            }
            TransactionIndexEntity entity = transactionIndexService.findByTransactionHash(hash);
            TransactionIndex transactionIndex = entity != null ? new TransactionIndex(entity.getBlockHash(), entity.getTransactionHash(), entity.getTransactionIndex()) : null;
            if (transactionIndex != null) {
                LOGGER.info("the transaction is exist in block with hash {}", hash);
                return;
            }
            boolean valid = verifyTransactionInputAndOutputInfo(tx, null);
            if (!valid) {
                LOGGER.info("the transaction is not valid {}", tx);
                return;
            }
            txCacheManager.addTransaction(tx);
            broadcastTransaction(tx);
        }
    }

    @Override
    public boolean hasStake(String address, SystemCurrencyEnum currency) {
        List<UTXO> result = getBestUTXOList(address, currency.getCurrency());
        return getUTXOCurrency(result, currency);
    }

    @Override
    public boolean hasStake(String preBlockHash, String address, SystemCurrencyEnum currency) {
        List<UTXO> result = utxoServiceProxy.getUnionUTXO(preBlockHash, address, currency.getCurrency());
        return getUTXOCurrency(result, currency);
    }

    @Override
    public Set<String> getRemovedMiners(Transaction tx) {
        Set<String> result = new HashSet<>();
        List<TransactionInput> inputs = tx.getInputs();
        if (CollectionUtils.isEmpty(inputs)) {
            return result;
        }
        for (TransactionInput input : inputs) {
            TransactionOutPoint prevOutPoint = input.getPrevOut();

            String txHash = prevOutPoint.getHash();
            TransactionIndexEntity entity = transactionIndexService.findByTransactionHash(txHash);
            TransactionIndex transactionIndex = entity != null ? new TransactionIndex(entity.getBlockHash(), entity.getTransactionHash(), entity.getTransactionIndex()) : null;
            if (transactionIndex == null) {
                continue;
            }

            String blockHash = transactionIndex.getBlockHash();
            Block block = blockService.getBlockByHash(blockHash);
            Transaction transactionByHash = block.getTransactionByHash(txHash);
            short index = prevOutPoint.getIndex();
            TransactionOutput preOutput = null;
            if (transactionByHash != null) {
                preOutput = transactionByHash.getTransactionOutputByIndex(index);
            }
            if (preOutput == null || !preOutput.isMinerCurrency()) {
                continue;
            }
            String address = preOutput.getLockScript().getAddress();
            if (result.contains(address)) {
                continue;
            }
            if (!hasStake(address, SystemCurrencyEnum.MINER)) {
                result.add(address);
            }
        }
        return result;
    }

    @Override
    public Set<String> getAddedMiners(Transaction tx) {
        Set<String> result = new HashSet<>();
        List<TransactionOutput> outputs = tx.getOutputs();
        for (int i = 0; i < outputs.size(); i++) {
            if (!outputs.get(i).isMinerCurrency()) {
                continue;
            }

            UTXO utxo = null;
            utxo = utxoServiceProxy.getUTXOOnBestChain(UTXO.buildKey(tx.getHash(), (short) i));
            if (utxo == null) {
                LOGGER.warn("cannot find utxo when get added miners, tx={},i={}", tx.getHash(), i);
                continue;
            }
            String address = utxo.getAddress();
            if (result.contains(address)) {
                continue;
            }
            if (hasStake(address, SystemCurrencyEnum.MINER)) {
                result.add(address);
            }
        }
        return result;
    }


    public boolean getUTXOCurrency(List<UTXO> result, SystemCurrencyEnum currency) {
        Money stakeMinMoney = new Money("1", currency.getCurrency());
        Money money = new Money("0", currency.getCurrency());
        for (UTXO utxo : result) {
            money.add(utxo.getOutput().getMoney());
            if (money.compareTo(stakeMinMoney) >= 0) {
                return true;
            }
        }
        return false;
    }

    public List<UTXO> getBestUTXOList(String address, String currency) {
        List<UTXOEntity> utxoEntities = bestUtxoService.findByLockScriptAndCurrency(address, currency);
        List<UTXO> utxos = Lists.newArrayList();
        utxoEntities.forEach(entity -> {
            Money money = new Money(entity.getAmount(), entity.getCurrency());
            LockScript lockScript = new LockScript();
            lockScript.setAddress(entity.getLockScript());
            lockScript.setType((short) entity.getScriptType());
            TransactionOutput output = new TransactionOutput();
            output.setMoney(money);
            output.setLockScript(lockScript);

            UTXO utxo = new UTXO();
            utxo.setHash(entity.getTransactionHash());
            utxo.setIndex((short) entity.getOutIndex());
            utxo.setAddress(entity.getLockScript());
            utxo.setOutput(output);
            utxos.add(utxo);
        });
        return utxos;
    }

    /**
     * validate tx
     *
     * @param tx    one tx
     * @param block current block
     * @return return result
     */

    public boolean verifyTransactionInputAndOutputInfo(Transaction tx, Block block) {
        if (null == tx) {
            LOGGER.error("transaction is null");
            return false;
        }
        int version = tx.getVersion();
        if (version < 0) {
            return false;
        }
        List<TransactionInput> inputs = tx.getInputs();
        List<TransactionOutput> outputs = tx.getOutputs();
        String hash = tx.getHash();
        if (!tx.sizeAllowed()) {
            LOGGER.info("Size of the transaction is illegal.");
            return false;
        }

        String blockHash = block != null ? block.getHash() : null;
        String preBlockHash = block != null ? block.getPrevBlockHash() : null;
        Map<String, Money> preMoneyMap = new HashMap<>(8);
        HashSet<String> prevOutKey = new HashSet<>();
        for (TransactionInput input : inputs) {
            if (!input.valid()) {
                LOGGER.info("input is invalid");
                return false;
            }
            String key = input.getPrevOut().getKey();
            boolean notContains = prevOutKey.add(key);
            if (!notContains) {
                LOGGER.info("the input has been spend in this transaction or in the other transaction in the block,tx hash {}, the block hash {}"
                        , tx.getHash()
                        , blockHash);
                return false;
            }
            TransactionOutput preOutput = getPreOutput(preBlockHash, input);
            if (preOutput == null) {
                LOGGER.info("pre-output is empty,input={},preOutput={},tx hash={},block hash={}", input, preOutput, tx.getHash(), blockHash);
                return false;
            }

            String currency = preOutput.getMoney().getCurrency();
            if (!preMoneyMap.containsKey(currency)) {
                preMoneyMap.put(currency, new Money("0", currency).add(preOutput.getMoney()));
            } else {
                preMoneyMap.put(currency, preMoneyMap.get(currency).add(preOutput.getMoney()));
            }
        }

        Map<String, Money> curMoneyMap = new HashMap<>(8);
        for (TransactionOutput output : outputs) {
            if (!output.valid()) {
                LOGGER.info("Current output is invalid");
                return false;
            }

            String currency = output.getMoney().getCurrency();
            if (!curMoneyMap.containsKey(currency)) {
                curMoneyMap.put(currency, new Money("0", currency).add(output.getMoney()));
            } else {
                curMoneyMap.put(currency, curMoneyMap.get(currency).add(output.getMoney()));
            }
        }

        if (preMoneyMap.keySet().size() != curMoneyMap.keySet().size()) {
            LOGGER.info("Pre-output currency type different from current");
            return false;
        }

        for (String key : curMoneyMap.keySet()) {
            Money preMoney = preMoneyMap.get(key);
            Money curMoney = curMoneyMap.get(key);

            if (preMoney == null) {
                LOGGER.info("Pre-output currency is null {}", key);
                return false;
            }

            if (curMoney == null) {
                LOGGER.info("Current output currency is null {}", key);
                return false;
            }
            LOGGER.info("input money :{}, output money:{}", preMoney.getValue(), curMoney.getValue());
            if (StringUtils.equals(SystemCurrencyEnum.CAS.getCurrency(), key)) {
                if (block == null) {
                    curMoney.add(transactionFeeService.getCurrencyFee(tx));
                }

                if (preMoney.compareTo(curMoney) < 0) {
                    LOGGER.info("Not enough cas fees");
                    return false;
                }
            } else {
                //TODO this ‘else’ is unnecessary, below code should be a precondition then  moved ahead ;commented by huangshengli 2018-05-28
                if (preMoney.compareTo(curMoney) < 0) {
                    LOGGER.info("Not enough fees, currency type: ", key);
                    return false;
                }
            }
        }

        return verifyInputs(inputs, hash, preBlockHash);
    }

    /**
     * validate inputs
     *
     * @param inputs
     * @param hash
     * @param preBlockHash
     * @return
     */
    private boolean verifyInputs(List<TransactionInput> inputs, String hash, String preBlockHash) {
        int size = inputs.size();
        TransactionInput input = null;
        UnLockScript unLockScript = null;
        for (int i = 0; i < size; i++) {
            input = inputs.get(i);
            if (null == input) {
                LOGGER.info("the input is empty {}", i);
                return false;
            }
            unLockScript = input.getUnLockScript();
            if (null == unLockScript) {
                LOGGER.info("the unLockScript is empty {}", i);
                return false;
            }

            String preUTXOKey = input.getPreUTXOKey();
            UTXO utxo = utxoServiceProxy.getUnionUTXO(preBlockHash, preUTXOKey);
            if (utxo == null) {
                LOGGER.info("there is no such utxokey={},preBlockHash={}", preUTXOKey, preBlockHash);
                return false;
            }

            List<String> sigList = unLockScript.getSigList();
            List<String> pkList = unLockScript.getPkList();
            if (CollectionUtils.isEmpty(sigList) || CollectionUtils.isEmpty(pkList)) {
                return false;
            }
            for (String sig : sigList) {
                boolean result = pkList.parallelStream().anyMatch(pubKey -> ECKey.verifySign(hash, sig, pubKey));
                if (!result) {
                    //can not find a pubKey to verify the sig with transaction hash
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * get input utxo
     *
     * @param input tx input
     * @return tx input ref output
     */
    private TransactionOutput getPreOutput(String preBlockHash, TransactionInput input) {
        String preOutKey = input.getPrevOut().getKey();
        if (StringUtils.isEmpty(preOutKey)) {
            LOGGER.info("preOutKey is empty,input={}", input.toJson());
            return null;
        }

        UTXO utxo;
        utxo = utxoServiceProxy.getUnionUTXO(preBlockHash, preOutKey);

        if (utxo == null) {
            LOGGER.warn("UTXO is empty,input={},preOutKey={}", input.toJson(), preOutKey);
            return null;
        }
        TransactionOutput output = utxo.getOutput();
        return output;
    }

    /**
     * validate coin base tx
     *
     * @param tx    one transaction
     * @param block current block
     * @return validate success return true else false
     */
    public boolean validCoinBaseTx(Transaction tx, Block block) {
        List<TransactionOutput> outputs = tx.getOutputs();
        if (CollectionUtils.isEmpty(outputs)) {
            LOGGER.info("Producer coinbase transaction: Outputs is empty,tx hash={},block hash={}", tx.getHash(), block.getHash());
            return false;
        }

        final int outputSize = outputs.size();
        if (MIN_OUTPUT_SIZE != outputSize) {
            LOGGER.info("Coinbase outputs number is less than twelve,tx hash={},block hash={}", tx.getHash(), block.getHash());
            return false;
        }

        Block preBlock = blockService.getBlockByHash(block.getPrevBlockHash());
        String preBlockHash = block.getPrevBlockHash();
        if (preBlock == null) {
            LOGGER.info("preBlock == null,tx hash={},block hash={}", tx.getHash(), block.getHash());
            return false;
        }
        if (!validPreBlock(preBlock, block.getHeight())) {
            LOGGER.info("pre block is not last best block,tx hash={},block hash={}", tx.getHash(), block.getHash());
            return false;
        }

        SortResult sortResult = transactionFeeService.orderTransaction(preBlockHash, block.getTransactions().subList(1, block.getTransactions().size()));
        Rewards rewards = transactionFeeService.countMinerAndWitnessRewards(sortResult.getFeeMap(), block.getHeight());
        //verify count coin base output
        if (!transactionFeeService.checkCoinBaseMoney(tx, rewards.getTotalMoney())) {
            LOGGER.info("verify miner coin base add witness not == total money totalMoney:{}", rewards.getTotalMoney());
            return false;
        }

        //verify producer coinbase output
        if (!validateProducerOutput(outputs.get(0), rewards.getMinerTotal())) {
            LOGGER.info("verify miner coinbase output failed,tx hash={},block hash={}", tx.getHash(), block.getHash());
            return false;
        }
        //verify witness reward money
        if (!rewards.getTopTenSingleWitnessMoney().checkRange() && !rewards.getLastWitnessMoney().checkRange()) {
            LOGGER.info("Producer coinbase transaction: topTenSingleWitnessMoney is error,topTenSingleWitnessMoney={} and lastWitnessMoney is error,lastWitnessMoney={}", rewards.getTopTenSingleWitnessMoney().getValue(), rewards.getLastWitnessMoney().getValue());
            return false;
        }
        //verify reward count
        if (!validateWitnessRewards(outputs, rewards.getTopTenSingleWitnessMoney(), rewards.getLastWitnessMoney())) {
            LOGGER.info("Validate witness reward failed");
            return false;
        }

        return true;
    }

    /**
     * validate previous block
     *
     * @param preBlock previous block
     * @param height   current height
     * @return return result
     */
    public boolean validPreBlock(Block preBlock, long height) {
        boolean isEffective = false;
        if (null == preBlock) {
            LOGGER.info("null == preBlock, height={}", height);
            return false;
        }
        if (0 >= height || height > Long.MAX_VALUE) {
            LOGGER.info("height is not correct, preBlock hash={},height={}", preBlock.getHash(), height);
            return false;
        }
        if ((preBlock.getHeight() + 1) != height) {
            LOGGER.info("(preBlock.getHeight() + 1) != height, preBlock hash={},height={}", preBlock.getHash(), height);
            return false;
        }

        BlockIndex blockIndex = blockIndexService.getBlockIndexByHeight(preBlock.getHeight());
        if (null == blockIndex) {
            LOGGER.info("null == blockIndex, preBlock hash={},height={}", preBlock.getHash(), height);
            return false;
        }

        List<String> blockHashs = blockIndex.getBlockHashs();
        if (CollectionUtils.isEmpty(blockHashs)) {
            LOGGER.info("the height is {} do not have List<String>", preBlock.getHeight());
            return false;
        }
        for (String hash : blockHashs) {
            if (StringUtils.equals(hash, preBlock.getHash())) {
                isEffective = true;
                LOGGER.info("isEffective = true");
                break;
            }
        }

        return isEffective;
    }


    /**
     * validate producer
     *
     * @param output      producer reward  output
     * @param totalReward total reward
     * @return return validate result
     */
    public boolean validateProducerOutput(TransactionOutput output, Money totalReward) {
        if (!totalReward.checkRange()) {
            LOGGER.info("Producer coinbase transaction: totalReward is error,totalReward={}", totalReward.getValue());
            return false;
        }
        if (null == output) {
            LOGGER.info("Producer coinbase transaction: UnLock script is null, output={},totalReward={}", output, totalReward.getValue());
            return false;
        }

        LockScript script = output.getLockScript();
        if (script == null) {
            LOGGER.info("Producer coinbase transaction: Lock script is null, output={},totalReward={}", output, totalReward.getValue());
            return false;
        }

        if (!SystemCurrencyEnum.CAS.getCurrency().equals(output.getMoney().getCurrency())) {
            LOGGER.info("Invalid producer coinbase transaction: Currency is not cas, output={},totalReward={}", output, totalReward.getValue());
            return false;
        }

        if (!validateProducerReward(output, totalReward)) {
            LOGGER.info("Validate producer reward failed, output={},totalReward={}", output, totalReward.getValue());
            return false;
        }

        return true;
    }

    /**
     * validate producer reward
     *
     * @param output      producer reward  output
     * @param totalReward reward
     * @return if coin base producer output money == count producer reward money return true else false
     */
    private boolean validateProducerReward(TransactionOutput output, Money totalReward) {
        if (!totalReward.checkRange()) {
            LOGGER.info("Producer coinbase transaction: totalReward is error,totalReward={}", totalReward);
            return false;
        }

        return output.getMoney().compareTo(totalReward) == 0;

    }


    /**
     * validate witness rewards
     *
     * @param outputs                  witness reward  outputs
     * @param topTenSingleWitnessMoney 10 of 11   reward
     * @param lastWitnessMoney         1 of 11    reward
     * @return if count outputs money == （topTenSingleWitnessMoney*10+lastWitnessMoney） return true else false
     */
    private boolean validateWitnessRewards(List<TransactionOutput> outputs, Money topTenSingleWitnessMoney, Money lastWitnessMoney) {

        Money witnessTotalMoney = new Money("0");
        outputs.forEach(output -> {
            witnessTotalMoney.add(output.getMoney());
        });
        Money countWitnessMoney = new Money(topTenSingleWitnessMoney.getValue()).multiply(witnessService.getWitnessSize() - 1).add(lastWitnessMoney);

        return countWitnessMoney.compareTo(witnessTotalMoney) == 0;
    }

    /**
     * received transaction if validate success and board
     *
     * @param tx received tx
     */
    public void broadcastTransaction(Transaction tx) {
        messageCenter.broadcast(tx);
        LOGGER.info("broadcast transaction success: {}", tx.getHash());
    }
}