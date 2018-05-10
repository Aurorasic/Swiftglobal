package com.higgsblock.global.chain.app.blockchain.transaction;

import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Lists;
import com.higgsblock.global.chain.app.blockchain.Block;
import com.higgsblock.global.chain.app.blockchain.BlockIndex;
import com.higgsblock.global.chain.app.blockchain.BlockService;
import com.higgsblock.global.chain.app.blockchain.BlockWitness;
import com.higgsblock.global.chain.app.blockchain.listener.MessageCenter;
import com.higgsblock.global.chain.app.script.LockScript;
import com.higgsblock.global.chain.app.script.UnLockScript;
import com.higgsblock.global.chain.common.enums.SystemCurrencyEnum;
import com.higgsblock.global.chain.common.utils.Money;
import com.higgsblock.global.chain.crypto.ECKey;
import com.higgsblock.global.chain.crypto.KeyPair;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.*;
import java.util.concurrent.ConcurrentMap;

/**
 * @author baizhengwen
 * @date 2018/2/24
 */
@Service
@Slf4j
public class TransactionService {
    private static final int MAX_WITNESS_NUM = 7;

    public final static List<String> WITNESS_ADDRESS_LIST = BlockService.WITNESS_ADDRESS_LIST;
    private static final int WITNESS_SIZE = WITNESS_ADDRESS_LIST.size();
    private static final int MIN_OUTPUT_SIZE = WITNESS_SIZE + 1; //11+1

    @Autowired
    private ConcurrentMap<String, TransactionIndex> transactionIndexData;
    @Autowired
    private ConcurrentMap<Long, BlockIndex> blockIndexData;
    @Autowired
    private ConcurrentMap<String, Block> blockData;
    @Autowired
    private BlockService blockService;
    @Autowired
    private KeyPair peerKeyPair;
    @Autowired
    private TransactionCacheManager txCacheManager;
    @Autowired
    private MessageCenter messageCenter;
    @Resource(name = "utxoData")
    private ConcurrentMap<String, UTXO> utxoMap;

    public static boolean checkSig(String txHash, LockScript lockScript, UnLockScript unLockScript) {
        short type = lockScript.getType();
        List<String> pkList = unLockScript.getPkList();
        List<String> sigList = unLockScript.getSigList();

        boolean valid = unLockScript.valid();
        if (!valid) {
            return valid;
        }

        if (type == ScriptTypeEnum.P2PKH.getType()) {
            if (sigList.size() != 1 || pkList.size() != 1) {
                return false;
            }
            String pubKey = pkList.get(0);
            String signature = sigList.get(0);
            if (!ECKey.checkPubKeyAndAddr(pubKey, lockScript.getAddress())) {
                return false;
            }
            if (!ECKey.verifySign(txHash, signature, pubKey)) {
                return false;
            }
        } else if (type == ScriptTypeEnum.P2SH.getType()) {
            //TODO yuguojia verify <P2SH> : hash(<2 pk1 pk2 pk3 3>) = p2sh
            for (int i = 0; i < sigList.size(); i++) {
                String pubKey = pkList.get(i);
                String signature = sigList.get(i);
                if (!ECKey.verifySign(txHash, signature, pubKey)) {
                    return false;
                }
            }
        }
        return false;
    }

    public boolean validCoinBaseTx(Transaction tx, Block block) {
        List<TransactionOutput> outputs = tx.getOutputs();
        if (CollectionUtils.isEmpty(outputs)) {
            LOGGER.error("Producer coinbase transaction: Outputs is empty,tx hash={}_block hash={}", tx.getHash(), block.getHash());
            return false;
        }

        final int outputSize = outputs.size();
        if (block.getHeight() > 14 && MIN_OUTPUT_SIZE != outputSize) {
            LOGGER.error("Coinbase outputs number is less than twelve,tx hash={}_block hash={}", tx.getHash(), block.getHash());
            return false;
        }

        Block preBlock = blockService.getBlock(block.getPrevBlockHash());
        if (preBlock == null) {
            LOGGER.error("preBlock == null,tx hash={}_block hash={}", tx.getHash(), block.getHash());
            return false;
        }
        if (!validPreBlock(preBlock, block.getHeight())) {
            LOGGER.error("pre block is not last best block,tx hash={}_block hash={}", tx.getHash(), block.getHash());
            return false;
        }

        Money totalReward = getTotalTransactionsRewards(preBlock);

        //verify producer coinbase output
        if (!validateProducerOutput(outputs.get(0), preBlock, totalReward)) {
            LOGGER.error("verify miner coinbase output failed,tx hash={}_block hash={}", tx.getHash(), block.getHash());
            return false;
        }
        if (outputSize > 1) {
            return validateWitnessOutputs(outputs.subList(1, outputSize), preBlock, totalReward);
        }
        return true;
    }

    public boolean validPreBlock(Block preBlock, long height) {
        boolean isEffective = false;
        if (null == preBlock) {
            LOGGER.error("null == preBlock, height={}", height);
            return false;
        }
        if (0 >= height || height > Long.MAX_VALUE) {
            LOGGER.error("height is not correct, preBlock hash={}_height={}", preBlock.getHash(), height);
            return false;
        }
        if ((preBlock.getHeight() + 1) != height) {
            LOGGER.error("(preBlock.getHeight() + 1) != height, preBlock hash={}_height={}", preBlock.getHash(), height);
            return false;
        }

        BlockIndex blockIndex = blockService.getBlockIndexByHeight(preBlock.getHeight());
        if (null == blockIndex) {
            LOGGER.error("null == blockIndex, preBlock hash={}_height={}", preBlock.getHash(), height);
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

    public boolean validateProducerOutput(TransactionOutput output, Block preBlock, Money totalReward) {
        if (!totalReward.checkRange()) {
            LOGGER.error("Producer coinbase transaction: totalReward is error,totalReward={}", totalReward.getValue());
            return false;
        }
        if (null == output || null == preBlock) {
            LOGGER.error("Producer coinbase transaction: UnLock script is null, output={}_totalReward={}", output, totalReward.getValue());
            return false;
        }

        LockScript script = output.getLockScript();
        if (script == null) {
            LOGGER.error("Producer coinbase transaction: Lock script is null, output={}_preBlock hash={}_totalReward={}", output, preBlock.getHash(), totalReward.getValue());
            return false;
        }

        BlockWitness signature = preBlock.getMinerFirstPKSig();
        String preBlockProducerAddr = signature.getAddress();
        String curProducerAddr = script.getAddress();

        if (!preBlockProducerAddr.equals(curProducerAddr)) {
            LOGGER.error("Invalid producer coinbase transaction: Address not match, output={}_preBlock hash={}_totalReward={}", output, preBlock.getHash(), totalReward.getValue());
            return false;
        }

        if (!SystemCurrencyEnum.CAS.getCurrency().equals(output.getMoney().getCurrency())) {
            LOGGER.error("Invalid producer coinbase transaction: Currency is not cas, output={}_preBlock hash={}_totalReward={}", output, preBlock.getHash(), totalReward.getValue());
            return false;
        }

        if (!validateProducerReward(output, preBlock, totalReward)) {
            LOGGER.error("Validate producer reward failed, output={}_preBlock hash={}_totalReward={}", output, preBlock.getHash(), totalReward.getValue());
            return false;
        }

        return true;
    }


    private boolean validateProducerReward(TransactionOutput output, Block preBlock, Money totalReward) {
        if (!totalReward.checkRange()) {
            LOGGER.error("Producer coinbase transaction: totalReward is error,totalReward={}", totalReward);
            return false;
        }

        List<BlockWitness> signatures = preBlock.getOtherWitnessSigPKS();
        if (CollectionUtils.isEmpty(signatures)) {
            Money preReward = new Money("0.1").multiply(totalReward);
            if (preReward.compareTo(output.getMoney()) != 0) {
                LOGGER.error("Producer reward is invalid, output={}_preBlock hash={}_totalReward_value={}_currency={} ", output, preBlock.getHash(), totalReward.getValue(), totalReward.getCurrency());
                return false;
            }
        } else {
            Money preReward = new Money("0.5").multiply(totalReward);
            if (preReward.compareTo(output.getMoney()) != 0) {
                LOGGER.error("Producer reward is invalid, output={}_preBlock hash={}_totalReward={}_value={}_currency={}", output, preBlock.getHash(), totalReward.getValue(), totalReward.getCurrency());
                return false;
            }
        }

        return true;
    }

    public boolean validateWitnessOutputs(List<TransactionOutput> outputs, Block preBlock, Money totalReward) {
        if (!totalReward.checkRange()) {
            LOGGER.error("Producer coinbase transaction: totalReward is error,totalReward={}", totalReward.getValue());
            return false;
        }

        if (null == preBlock) {
            LOGGER.error("outputs or preBlock is null");
            return false;
        }

        if (CollectionUtils.isEmpty(outputs)) {
            LOGGER.error("Witness coinbase transaction: Outputs is empty, outputs={}_preBlock hash={}_totalReward={}", outputs, preBlock.getHash(), totalReward.getValue());
            return false;
        }

        List<BlockWitness> signatures = preBlock.getOtherWitnessSigPKS();
        if (CollectionUtils.isEmpty(signatures) || signatures.size() < MAX_WITNESS_NUM) {
            LOGGER.error("Witness coinbase transaction: Pre-signatures are invalid, outputs={}_preBlock hash={}_totalReward={}", outputs, preBlock.getHash(), totalReward.getValue());
            return false;
        }

        int size = signatures.size();
        for (int i = 0; i < size; i++) {
            BlockWitness signature = signatures.get(i);
            String preAddr = signature.getAddress();
            TransactionOutput output = outputs.get(i);
            LockScript script = output.getLockScript();
            if (script == null) {
                LOGGER.error("Witness coinbase transaction: Lock script is null, outputs={}_preBlock hash={}_totalReward={}", outputs, preBlock.getHash(), totalReward.getValue());
                return false;
            }

            if (!WITNESS_ADDRESS_LIST.contains(script.getAddress())) {
                LOGGER.error("Witness address not in list");
                return false;
            }

            if (StringUtils.isEmpty(output.getMoney().getCurrency())) {
                LOGGER.error("Invalid Witness coinbase transaction: Currency is null, outputs={}_preBlock hash={}_totalReward={}", outputs, preBlock.getHash(), totalReward.getValue());
                return false;
            }

            if (!SystemCurrencyEnum.CAS.getCurrency().equals(output.getMoney().getCurrency())) {
                LOGGER.error("Invalid Witness coinbase transaction: Currency is not cas, outputs={}_preBlock hash={}_totalReward={}", outputs, preBlock.getHash(), totalReward.getValue());
                return false;
            }
        }

        if (!validateWitnessRewards(outputs, totalReward)) {
            LOGGER.error("Validate witness reward failed");
            return false;
        }

        return true;
    }

    private boolean validateWitnessRewards(List<TransactionOutput> outputs, Money totalReward) {
        if (!totalReward.checkRange()) {
            LOGGER.error("Pre-block all transactions' reward is error");
            return false;
        }

        Money preReward = new Money("0.5").multiply(totalReward);
        Money witnessReward = preReward.divide(WITNESS_SIZE);

        outputs.stream().forEach(output -> {
            if (witnessReward.compareTo(output.getMoney()) != 0) {
                LOGGER.error("Witness reward is invalid");
            }
        });

        return true;
    }

    public boolean verifyTransaction(Transaction tx, HashSet<String> prevOutKey, Block block) {
        short version = tx.getVersion();
        if (version < 0) {
            return false;
        }
        List<TransactionInput> inputs = tx.getInputs();
        List<TransactionOutput> outputs = tx.getOutputs();
        String hash = tx.getHash();
//        boolean checkInput = block == null || !block.isgenesisBlock();
//        if (checkInput && CollectionUtils.isEmpty(inputs)) {
//            LOGGER.error("inputs is empty");
//            return false;
//        }
//        if (CollectionUtils.isEmpty(outputs)) {
//            LOGGER.error("outputs is empty");
//            return false;
//        }
        if (!tx.sizeAllowed()) {
            LOGGER.error("Size of the transaction is illegal.");
            return false;
        }

        String blockHash = block != null ? block.getHash() : null;
        Map<String, Money> preMoneyMap = new HashMap<>();
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
            TransactionOutput preOutput = getPreOutput(input);
            if (preOutput == null) {
                LOGGER.error("pre-output is empty,input={}_preOutput={}_tx hash={}_block hash={}", input, preOutput, tx.getHash(), blockHash);
                return false;
            }

            String currency = preOutput.getMoney().getCurrency();
            if (!preMoneyMap.containsKey(currency)) {
                preMoneyMap.put(currency, new Money("0", currency).add(preOutput.getMoney()));
            } else {
                preMoneyMap.put(currency, preMoneyMap.get(currency).add(preOutput.getMoney()));
            }
        }

        Map<String, Money> curMoneyMap = new HashMap<>();
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

            if (StringUtils.equals(SystemCurrencyEnum.CAS.getCurrency(), key)) {
                curMoney.add(getCurrencyFee(""));
                if (preMoney.compareTo(curMoney) < 0) {
                    LOGGER.error("Not enough cas fees");
                    return false;
                }
            } else {
                if (preMoney.compareTo(curMoney) < 0) {
                    LOGGER.error("Not enough fees, currency type: ", key);
                    return false;
                }
            }
        }

        return verifyInputs(inputs, hash);
    }

    private TransactionOutput getPreOutput(TransactionInput input) {
        String preOutKey = input.getPrevOut().getKey();
        if (StringUtils.isEmpty(preOutKey)) {
            LOGGER.warn("ipreOutKey is empty,input={}", JSONObject.toJSONString(input, true));
            return null;
        }

        UTXO utxo = utxoMap.get(preOutKey);

        if (utxo == null) {
            LOGGER.warn("UTXO is empty,input={}_preOutKey={}", JSONObject.toJSONString(input, true), preOutKey);
            return null;
        }
        TransactionOutput output = utxo.getOutput();
        return output;
    }

    //TODO: zhao xiaogang currently all currency return one value
    private Money getCurrencyFee(String currency) {
        return new Money("0.001");
    }

    private boolean verifyInputs(List<TransactionInput> inputs, String hash) {
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
            TransactionIndex preTxIndex = transactionIndexData.get(input.getPrevOut().getHash());
            if (preTxIndex == null) {
                //if the transactionIndex is not exist,
                //so local data is not right
                LOGGER.error("the preTxIndex is empty {}", i);
                return false;
            }
            Map<Short, String> outsSpend = preTxIndex.getOutsSpend();
            if (MapUtils.isEmpty(outsSpend)) {
                //if the outsSpend is empty;
                //this transaction's transactions have not been used
                continue;
            }
            short index = input.getPrevOut().getIndex();
            boolean spent = preTxIndex.isSpent(index);
            String txHash = preTxIndex.getTxHash();
            if (spent) {
                String blockHash = preTxIndex.getBlockHash();
                Block block = blockData.get(blockHash);
                if (block == null) {
                    LOGGER.error("the block is empty {}", i);
                    return false;
                }
                BlockIndex blockIndex = blockIndexData.get(block.getHeight());
                if (blockIndex == null) {
                    LOGGER.error("the blockIndex is empty {}", i);
                    //if the blockIndex is not exist,
                    //so local data is not right
                    return false;
                }
                boolean best = blockIndex.isBest(txHash);
                if (best) {
                    LOGGER.error("the blockIndex is empty {}", i);
                    return false;
                }
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

    public Transaction buildCoinBaseTx(long lockTime, short version, long height) {
        LOGGER.info("begin to build coinBase transaction");
        //BlockIndex lastBlockIndex = blockService.getLastBlockIndex();
        BlockIndex lastBlockIndex = blockService.getBlockIndexByHeight(height - 1);
        if (lastBlockIndex == null) {
            throw new RuntimeException("can not find last blockIndex");
        }

        String blockHash = lastBlockIndex.getBestBlockHash();
        LOGGER.info("the current height={} and blockHash={}", lastBlockIndex.getHeight(), blockHash);
        Block block = blockData.get(blockHash);
        if (block == null) {
            throw new RuntimeException("can not find block with blockHash " + blockHash);
        }

        BlockWitness minerPKSig = block.getMinerFirstPKSig();
        if (minerPKSig == null) {
            throw new RuntimeException("can not find miner PK sig from block");
        }

        Money totalRewards = getTotalTransactionsRewards(block);

        Transaction transaction = new Transaction();
        transaction.setCreatorPubKey(peerKeyPair.getPubKey());
        transaction.setVersion(version);
        transaction.setLockTime(lockTime);

        List<TransactionOutput> outputList = Lists.newArrayList();
        List<BlockWitness> witnessList = block.getOtherWitnessSigPKS();

        if (CollectionUtils.isEmpty(witnessList)) {
            //producer occupy 10%
            Money rewards = new Money("0.1").multiply(totalRewards);
            TransactionOutput output = generateTransactionOutput(minerPKSig.getAddress(), rewards);
            if (output != null) {
                outputList.add(output);
            }
        } else {
            //producer occupy 50%，others for witness
            Money rewards = new Money("0.5").multiply(totalRewards);
            LOGGER.info("buildCoinBaseTx's rewards is {}", rewards.getValue());
            String producerAddr = minerPKSig.getAddress();
            TransactionOutput produerCoinbaseOutput = generateTransactionOutput(producerAddr, rewards);
            List<TransactionOutput> witnessCoinbaseOutput = genWitnessCoinbaseOutput(rewards);

            if (produerCoinbaseOutput != null) {
                outputList.add(produerCoinbaseOutput);
            }
            outputList.addAll(witnessCoinbaseOutput);
        }

        if (CollectionUtils.isEmpty(outputList)) {
            return null;
        }
        transaction.setOutputs(outputList);

        return transaction;
    }

//    private Transaction buildProducerCoinBaseTx(String address, Money producerReward, long lockTime, short version) {
//        Transaction transaction = new Transaction();
//        transaction.setVersion(version);
//        transaction.setLockTime(lockTime);
//
//        TransactionOutput transactionOutput = new TransactionOutput();
//        transactionOutput.setMoney(producerReward);
//        LockScript lockScript = new LockScript();
//        lockScript.setAddress(address);
//        transactionOutput.setLockScript(lockScript);
//        List emptyList = Lists.newArrayList();
//        emptyList.add(transactionOutput);
//        transaction.setOutputs(emptyList);
//        return transaction;
//    }

    private Money getTotalTransactionsRewards(Block block) {
        Money totalMoney = new Money("1");

//        List<Transaction> transactions = block.getTransactions();
//        for (Transaction transaction : transactions) {
//            totalFee = totalFee.add(getOneTransactionFee(transaction));
//        }


        LOGGER.info("Transactions' total reward : {}", totalMoney.getValue());

//        Money percent = new Money("0.4"); //producer occupy rewards 40%
//        return totalMoney.multiply(percent.getAmount());

        return totalMoney;
    }

    private Money getOneTransactionFee(Transaction transaction) {
        List<TransactionInput> inputs = transaction.getInputs();
        List<TransactionOutput> outputs = transaction.getOutputs();

        Money preOutMoney = new Money("0");
        for (TransactionInput input : inputs) {
            String preOutKey = input.getPrevOut().getKey();
            UTXO utxo = utxoMap.get(preOutKey);
            TransactionOutput output = utxo.getOutput();
            if (output.isCASCurrency()) {
                preOutMoney.add(output.getMoney());
            }
        }

        LOGGER.info("Transactions' pre-output amount : {}", preOutMoney.getValue());

        Money outPutMoney = new Money("0");
        for (TransactionOutput output : outputs) {
            if (output.isCASCurrency()) {
                outPutMoney.add(output.getMoney());
            }
        }

        LOGGER.info("Transactions' output amount : {}", outPutMoney);

        return preOutMoney.subtract(outPutMoney);
    }

//    private Transaction buildWitnessCoinBaseTx(List<TransactionOutput> outputList, long lockTime, short version) {
//        Transaction transaction = new Transaction();
//        transaction.setVersion(version);
//        transaction.setLockTime(lockTime);
//        transaction.setOutputs(outputList);
//        return transaction;
//    }

    private List<TransactionOutput> genWitnessCoinbaseOutput(Money producerReward) {
        List<TransactionOutput> outputList = Lists.newArrayList();
        Money witnessReword = new Money(producerReward.getValue()).divide(WITNESS_SIZE);

        WITNESS_ADDRESS_LIST.forEach(address -> {
            TransactionOutput transactionOutput = generateTransactionOutput(address, witnessReword);
            outputList.add(transactionOutput);
        });

        return outputList;
    }

    public void receivedTransaction(Transaction tx) {
        String hash = tx.getHash();
        LOGGER.info("receive a new transaction from remote with hash {} and data {}", hash, tx);
        Map<String, Transaction> transactionMap = txCacheManager.getTransactionMap().asMap();
        if (transactionMap.containsKey(hash)) {
            LOGGER.info("the transaction is exist in cache with hash {}", hash);
            return;
        }
        TransactionIndex transactionIndex = transactionIndexData.get(hash);
        if (transactionIndex != null) {
            LOGGER.info("the transaction is exist in block with hash {}", hash);
            return;
        }
        HashSet<String> prevOutKey = new HashSet<>();
        boolean valid = this.verifyTransaction(tx, prevOutKey, null);
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
            TransactionIndex transactionIndex = transactionIndexData.get(txHash);
            String blockHash = transactionIndex.getBlockHash();
            Block block = blockService.getBlock(blockHash);
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
            if (!hasMinerStake(address)) {
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
            UTXO utxo = utxoMap.get(UTXO.buildKey(tx.getHash(), (short) i));
            if (utxo == null) {
                LOGGER.warn("cannot find utxo when get added miners, tx={}_i={}", tx.getHash(), i);
                continue;
            }
            String address = utxo.getAddress();
            if (result.contains(address)) {
                continue;
            }
            if (hasMinerStake(address)) {
                result.add(address);
            }
        }
        return result;
    }

    public List<UTXO> getUTXOList(String address, String currency) {
        List<UTXO> result = new LinkedList<>();
        Iterator<String> utxoIterator = utxoMap.keySet().iterator();
        while (utxoIterator.hasNext()) {
            String next = utxoIterator.next();
            UTXO utxo = utxoMap.get(next);
            if (StringUtils.equals(utxo.getOutput().getMoney().getCurrency(), currency) &&
                    StringUtils.equals(address, utxo.getAddress())) {
                result.add(utxo);
            }
        }
        return result;
    }

    /**
     * statics miner number
     *
     * @return
     */
    public long getMinerNumber() {
        long count = 0L;
        Iterator<String> utxoIterator = utxoMap.keySet().iterator();
        Set<String> addresses = new HashSet<>();
        while (utxoIterator.hasNext()) {
            String next = utxoIterator.next();
            UTXO utxo = utxoMap.get(next);
            if (utxo == null) {
                continue;
            }
            if (!SystemCurrencyEnum.MINER.getCurrency().equals(utxo.getOutput().getMoney().getCurrency())) {
                continue;
            }
            String address = utxo.getOutput().getLockScript().getAddress();
            addresses.add(address);
        }
        for (String address : addresses) {
            if (hasMinerStake(address)) {
                count++;
            }
        }
        return count;
    }

    public boolean hasMinerStake(String address) {
        String minerCurrency = SystemCurrencyEnum.MINER.getCurrency();
        Money minerStakeMinMoney = new Money("1", minerCurrency);
        List<UTXO> result = getUTXOList(address, minerCurrency);
        Money money = new Money("0", minerCurrency);
        for (UTXO utxo : result) {
            money.add(utxo.getOutput().getMoney());
            if (money.compareTo(minerStakeMinMoney) >= 0) {
                return true;
            }
        }
        return false;
    }

    public void broadcastTransaction(Transaction tx) {
        messageCenter.broadcast(tx);
        LOGGER.info("broadcast transaction success: " + tx.getHash());
    }

}
