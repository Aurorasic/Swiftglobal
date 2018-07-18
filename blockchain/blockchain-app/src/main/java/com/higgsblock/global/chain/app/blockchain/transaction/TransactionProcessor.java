package com.higgsblock.global.chain.app.blockchain.transaction;

import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Lists;
import com.higgsblock.global.chain.app.blockchain.Block;
import com.higgsblock.global.chain.app.blockchain.BlockIndex;
import com.higgsblock.global.chain.app.blockchain.BlockProcessor;
import com.higgsblock.global.chain.app.blockchain.listener.MessageCenter;
import com.higgsblock.global.chain.app.blockchain.script.LockScript;
import com.higgsblock.global.chain.app.blockchain.script.UnLockScript;
import com.higgsblock.global.chain.app.dao.ISpentTransactionOutIndexRepository;
import com.higgsblock.global.chain.app.dao.ITransactionIndexRepository;
import com.higgsblock.global.chain.app.dao.IUTXORepository;
import com.higgsblock.global.chain.app.dao.entity.SpentTransactionOutIndexEntity;
import com.higgsblock.global.chain.app.dao.entity.TransactionIndexEntity;
import com.higgsblock.global.chain.app.dao.entity.UTXOEntity;
import com.higgsblock.global.chain.app.service.UTXODaoServiceProxy;
import com.higgsblock.global.chain.app.service.impl.BlockIndexService;
import com.higgsblock.global.chain.app.service.impl.BlockPersistService;
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
 * @author baizhengwen
 * @date 2018/2/24
 */
@Service
@Slf4j
public class TransactionProcessor {

    /**
     * 11+1
     */
    private static final int MIN_OUTPUT_SIZE = 11 + 1;

    @Autowired
    private TransactionCacheManager txCacheManager;

    @Autowired
    private MessageCenter messageCenter;

    @Autowired
    private BlockPersistService blockPersistService;

    @Autowired
    private UTXODaoServiceProxy utxoDaoServiceProxy;

    @Autowired
    private IUTXORepository iutxoRepository;

    @Autowired
    private ITransactionIndexRepository iTransactionIndexRepository;

    @Autowired
    private BlockIndexService blockIndexService;

    @Autowired
    private TransactionFeeProcess transactionFeeProcess;

    @Autowired
    private ISpentTransactionOutIndexRepository spentTransactionOutIndexRepository;

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
            LOGGER.error("Producer coinbase transaction: Outputs is empty,tx hash={},block hash={}", tx.getHash(), block.getHash());
            return false;
        }

        final int outputSize = outputs.size();
        if (MIN_OUTPUT_SIZE != outputSize) {
            LOGGER.error("Coinbase outputs number is less than twelve,tx hash={},block hash={}", tx.getHash(), block.getHash());
            return false;
        }

        Block preBlock = blockPersistService.getBlockByHash(block.getPrevBlockHash());
        String preBlockHash = block.getPrevBlockHash();
        if (preBlock == null) {
            LOGGER.error("preBlock == null,tx hash={},block hash={}", tx.getHash(), block.getHash());
            return false;
        }
        if (!validPreBlock(preBlock, block.getHeight())) {
            LOGGER.error("pre block is not last best block,tx hash={},block hash={}", tx.getHash(), block.getHash());
            return false;
        }

        SortResult sortResult = transactionFeeProcess.orderTransaction(preBlockHash, block.getTransactions().subList(1, block.getTransactions().size()));
        TransactionFeeProcess.Rewards rewards = transactionFeeProcess.countMinerAndWitnessRewards(sortResult.getFeeMap(), block.getHeight());
        //verify count coin base output
        if (!transactionFeeProcess.checkCoinBaseMoney(tx, rewards.getTotalMoney())) {
            LOGGER.error("verify miner coin base add witness not == total money totalMoney:{}", rewards.getTotalMoney());
            return false;
        }

        //verify producer coinbase output
        if (!validateProducerOutput(outputs.get(0), rewards.getMinerTotal())) {
            LOGGER.error("verify miner coinbase output failed,tx hash={},block hash={}", tx.getHash(), block.getHash());
            return false;
        }

        return validateWitnessOutputs(outputs.subList(1, outputSize), rewards.getTopTenSingleWitnessMoney(), rewards.getLastWitnessMoney());
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
            LOGGER.error("null == preBlock, height={}", height);
            return false;
        }
        if (0 >= height || height > Long.MAX_VALUE) {
            LOGGER.error("height is not correct, preBlock hash={},height={}", preBlock.getHash(), height);
            return false;
        }
        if ((preBlock.getHeight() + 1) != height) {
            LOGGER.error("(preBlock.getHeight() + 1) != height, preBlock hash={},height={}", preBlock.getHash(), height);
            return false;
        }

        BlockIndex blockIndex = blockIndexService.getBlockIndexByHeight(preBlock.getHeight());
        if (null == blockIndex) {
            LOGGER.error("null == blockIndex, preBlock hash={},height={}", preBlock.getHash(), height);
            return false;
        }

        List<String> blockHashs = blockIndex.getBlockHashs();
        if (CollectionUtils.isEmpty(blockHashs)) {
            LOGGER.error("the height is {} do not have List<String>", preBlock.getHeight());
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
    public boolean validateProducerOutput(TransactionOutput output,  Money totalReward) {
        if (!totalReward.checkRange()) {
            LOGGER.error("Producer coinbase transaction: totalReward is error,totalReward={}", totalReward.getValue());
            return false;
        }
        if (null == output) {
            LOGGER.error("Producer coinbase transaction: UnLock script is null, output={},totalReward={}", output, totalReward.getValue());
            return false;
        }

        LockScript script = output.getLockScript();
        if (script == null) {
            LOGGER.error("Producer coinbase transaction: Lock script is null, output={},totalReward={}", output,  totalReward.getValue());
            return false;
        }

        if (!SystemCurrencyEnum.CAS.getCurrency().equals(output.getMoney().getCurrency())) {
            LOGGER.error("Invalid producer coinbase transaction: Currency is not cas, output={},totalReward={}", output,  totalReward.getValue());
            return false;
        }

        if (!validateProducerReward(output, totalReward)) {
            LOGGER.error("Validate producer reward failed, output={},totalReward={}", output,  totalReward.getValue());
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
            LOGGER.error("Producer coinbase transaction: totalReward is error,totalReward={}", totalReward);
            return false;
        }

        return output.getMoney().compareTo(totalReward) == 0;

    }

    /**
     * validate witness
     *
     * @param outputs                  witness reward  outputs
     * @param topTenSingleWitnessMoney 10 of 11   reward
     * @param lastWitnessMoney         1 of 11    reward
     * @return return validate result
     */
    public boolean validateWitnessOutputs(List<TransactionOutput> outputs, Money topTenSingleWitnessMoney, Money lastWitnessMoney) {
        if (!topTenSingleWitnessMoney.checkRange() && !lastWitnessMoney.checkRange()) {
            LOGGER.error("Producer coinbase transaction: topTenSingleWitnessMoney is error,topTenSingleWitnessMoney={} and lastWitnessMoney is error,lastWitnessMoney={}", topTenSingleWitnessMoney.getValue(), lastWitnessMoney.getValue());
            return false;
        }

        if (!validateWitnessRewards(outputs, topTenSingleWitnessMoney, lastWitnessMoney)) {
            LOGGER.error("Validate witness reward failed");
            return false;
        }

        return true;
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
        Money countWitnessMoney = new Money(topTenSingleWitnessMoney.getValue()).multiply(BlockProcessor.WITNESS_ADDRESS_LIST.size() - 1).add(lastWitnessMoney);

        return countWitnessMoney.compareTo(witnessTotalMoney) == 0;
    }

    /**
     * validate tx
     *
     * @param tx    one tx
     * @param block current block
     * @return return result
     */
    public boolean verifyTransaction(Transaction tx, Block block) {
        short version = tx.getVersion();
        if (version < 0) {
            return false;
        }
        List<TransactionInput> inputs = tx.getInputs();
        List<TransactionOutput> outputs = tx.getOutputs();
        String hash = tx.getHash();
        if (!tx.sizeAllowed()) {
            LOGGER.error("Size of the transaction is illegal.");
            return false;
        }

        String blockHash = block != null ? block.getHash() : null;
        String preBlockHash = block != null ? block.getPrevBlockHash() : null;
        Map<String, Money> preMoneyMap = new HashMap<>(8);
        HashSet<String> prevOutKey = new HashSet<>();
        for (TransactionInput input : inputs) {
            if (!input.valid()) {
                LOGGER.error("input is invalid");
                return false;
            }
            String key = input.getPrevOut().getKey();
            boolean notContains = prevOutKey.add(key);
            if (!notContains) {

                LOGGER.error("the input has been spend in this transaction or in the other transaction in the block,tx hash {}, the block hash {}"
                        , tx.getHash()
                        , blockHash);
                return false;
            }
            TransactionOutput preOutput = getPreOutput(preBlockHash, input);
            if (preOutput == null) {
                LOGGER.error("pre-output is empty,input={},preOutput={},tx hash={},block hash={}", input, preOutput, tx.getHash(), blockHash);
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
                LOGGER.error("Current output is invalid");
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
            LOGGER.error("Pre-output currency type different from current");
            return false;
        }

        for (String key : curMoneyMap.keySet()) {
            Money preMoney = preMoneyMap.get(key);
            Money curMoney = curMoneyMap.get(key);

            if (preMoney == null) {
                LOGGER.error("Pre-output currency is null {}", key);
                return false;
            }

            if (curMoney == null) {
                LOGGER.error("Current output currency is null {}", key);
                return false;
            }
            LOGGER.info("input money :{}, output money:{}", preMoney.getValue(), curMoney.getValue());
            if (StringUtils.equals(SystemCurrencyEnum.CAS.getCurrency(), key)) {
                if (block == null) {
                    curMoney.add(transactionFeeProcess.getCurrencyFee(tx));
                }

                if (preMoney.compareTo(curMoney) < 0) {
                    LOGGER.error("Not enough cas fees");
                    return false;
                }
            } else {
                //TODO this ‘else’ is unnecessary, below code should be a precondition then  moved ahead ;commented by huangshengli 2018-05-28
                if (preMoney.compareTo(curMoney) < 0) {
                    LOGGER.error("Not enough fees, currency type: ", key);
                    return false;
                }
            }
        }

        return verifyInputs(inputs, hash, preBlockHash);
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
            LOGGER.warn("ipreOutKey is empty,input={}", JSONObject.toJSONString(input, true));
            return null;
        }

        UTXO utxo;
        utxo = utxoDaoServiceProxy.getUnionUTXO(preBlockHash, preOutKey);

        if (utxo == null) {
            LOGGER.warn("UTXO is empty,input={},preOutKey={}", JSONObject.toJSONString(input, true), preOutKey);
            return null;
        }
        TransactionOutput output = utxo.getOutput();
        return output;
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
                LOGGER.error("the input is empty {}", i);
                return false;
            }
            unLockScript = input.getUnLockScript();
            if (null == unLockScript) {
                LOGGER.error("the unLockScript is empty {}", i);
                return false;
            }

            String preUTXOKey = input.getPreUTXOKey();
            UTXO utxo = utxoDaoServiceProxy.getUnionUTXO(preBlockHash, preUTXOKey);
            if (utxo == null) {
                LOGGER.error("there is no such utxokey={}_preBlockHash={}", preUTXOKey, preBlockHash);
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
     * step 1：validate transaction
     * step 2: broad transaction
     *
     * @param tx received tx
     */
    public void receivedTransaction(Transaction tx) {
        String hash = tx.getHash();
        LOGGER.info("receive a new transaction from remote with hash {} and data {}", hash, tx);
        Map<String, Transaction> transactionMap = txCacheManager.getTransactionMap().asMap();
        if (transactionMap.containsKey(hash)) {
            LOGGER.info("the transaction is exist in cache with hash {}", hash);
            return;
        }
        TransactionIndexEntity entity = iTransactionIndexRepository.findByTransactionHash(hash);
        TransactionIndex transactionIndex = entity != null ? new TransactionIndex(entity.getBlockHash(), entity.getTransactionHash(), entity.getTransactionIndex()) : null;
        if (transactionIndex != null) {
            LOGGER.info("the transaction is exist in block with hash {}", hash);
            return;
        }
        boolean valid = verifyTransaction(tx, null);
        if (!valid) {
            LOGGER.info("the transaction is not valid {}", tx);
            return;
        }
        txCacheManager.addTransaction(tx);
        broadcastTransaction(tx);
    }


    public Set<String> getRemovedMiners(Transaction tx) {
        Set<String> result = new HashSet<>();
        List<TransactionInput> inputs = tx.getInputs();
        if (CollectionUtils.isEmpty(inputs)) {
            return result;
        }
        for (TransactionInput input : inputs) {
            TransactionOutPoint prevOutPoint = input.getPrevOut();

            String txHash = prevOutPoint.getHash();
            TransactionIndexEntity entity = iTransactionIndexRepository.findByTransactionHash(txHash);
            TransactionIndex transactionIndex = entity != null ? new TransactionIndex(entity.getBlockHash(), entity.getTransactionHash(), entity.getTransactionIndex()) : null;
            if (transactionIndex == null) {
                continue;
            }

            String blockHash = transactionIndex.getBlockHash();
            Block block = blockPersistService.getBlockByHash(blockHash);
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

    public Set<String> getAddedMiners(Transaction tx) {
        Set<String> result = new HashSet<>();
        List<TransactionOutput> outputs = tx.getOutputs();
        for (int i = 0; i < outputs.size(); i++) {
            if (!outputs.get(i).isMinerCurrency()) {
                continue;
            }

            UTXO utxo = null;
            utxo = utxoDaoServiceProxy.getUTXOOnBestChain(UTXO.buildKey(tx.getHash(), (short) i));
            if (utxo == null) {
                LOGGER.warn("cannot find utxo when get added miners, tx={}_i={}", tx.getHash(), i);
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

    public List<UTXO> getUTXOList(String address, String currency) {
        List<UTXOEntity> utxoEntities = iutxoRepository.findByLockScriptAndCurrency(address, currency);
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

    public boolean hasStake(String address, SystemCurrencyEnum currency) {
        Money stakeMinMoney = new Money("1", currency.getCurrency());
        List<UTXO> result = getUTXOList(address, currency.getCurrency());
        Money money = new Money("0", currency.getCurrency());
        for (UTXO utxo : result) {
            money.add(utxo.getOutput().getMoney());
            if (money.compareTo(stakeMinMoney) >= 0) {
                return true;
            }
        }

        return false;
    }

    /**
     * received transaction if validate success and board
     *
     * @param tx received tx
     */
    public void broadcastTransaction(Transaction tx) {
        messageCenter.broadcast(tx);
        LOGGER.info("broadcast transaction success: " + tx.getHash());
    }

    private boolean isSpent(String preTxHash, short outIndex) {
        List<SpentTransactionOutIndexEntity> spentTxOutIndexEntities = spentTransactionOutIndexRepository.findByPreTransactionHash(preTxHash);
        if (CollectionUtils.isEmpty(spentTxOutIndexEntities)) {
            return false;
        }
        for (SpentTransactionOutIndexEntity spentTxOutIndexEntity : spentTxOutIndexEntities) {
            if (spentTxOutIndexEntity.getOutIndex() == outIndex) {
                return true;
            }
        }
        return false;
    }

}
