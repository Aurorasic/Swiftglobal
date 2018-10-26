package com.higgsblock.global.chain.app.service.impl;

import com.google.common.base.Charsets;
import com.google.common.hash.HashFunction;
import com.google.common.hash.Hashing;
import com.higgsblock.global.chain.app.blockchain.Block;
import com.higgsblock.global.chain.app.blockchain.BlockIndex;
import com.higgsblock.global.chain.app.blockchain.Rewards;
import com.higgsblock.global.chain.app.blockchain.listener.MessageCenter;
import com.higgsblock.global.chain.app.blockchain.script.LockScript;
import com.higgsblock.global.chain.app.blockchain.script.UnLockScript;
import com.higgsblock.global.chain.app.blockchain.transaction.*;
import com.higgsblock.global.chain.app.contract.BalanceUtil;
import com.higgsblock.global.chain.app.contract.RepositoryRoot;
import com.higgsblock.global.chain.app.dao.IContractRepository;
import com.higgsblock.global.chain.app.dao.entity.TransactionIndexEntity;
import com.higgsblock.global.chain.app.service.*;
import com.higgsblock.global.chain.common.enums.SystemCurrencyEnum;
import com.higgsblock.global.chain.common.utils.Money;
import com.higgsblock.global.chain.crypto.ECKey;
import com.higgsblock.global.chain.vm.api.ExecutionResult;
import com.higgsblock.global.chain.vm.config.ByzantiumConfig;
import com.higgsblock.global.chain.vm.core.Repository;
import com.higgsblock.global.chain.vm.core.SystemProperties;
import com.higgsblock.global.chain.vm.fee.FeeUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.map.HashedMap;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigInteger;
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

    public static final long WITNESS_NUM = 11;

    @Autowired
    private TransactionCacheManager txCacheManager;

    @Autowired
    private MessageCenter messageCenter;

    @Autowired
    private BlockService blockService;

    @Autowired
    private TransactionIndexService transactionIndexService;

    @Autowired
    private BlockIndexService blockIndexService;

    @Autowired
    private TransactionFeeService transactionFeeService;

    @Autowired
    private IWitnessService witnessService;

    @Autowired
    private IBalanceService balanceService;

    @Autowired
    private IContractService contractService;

    @Autowired
    private IContractRepository contractRepository;
    @Autowired
    private IIcoService icoService;
    @Autowired
    private UTXOServiceProxy utxoServiceProxy;
    @Autowired
    private ByzantiumConfig blockchainConfig;

    @Override
    public boolean validTransactions(Block block) {
        LOGGER.debug("begin to check the transactions of block {}", block.getHeight());

        //step1 verify block transaction is null
        List<Transaction> transactions = block.getTransactions();
        if (CollectionUtils.isEmpty(transactions)) {
            LOGGER.info("transactions is empty, block_hash={}", block.getHash());
            return false;
        }

        //step2 verify transaction size
        int txNumber = transactions.size();
        if (TRANSACTION_NUMBER > txNumber) {
            LOGGER.info("transactions number is less than two, block_hash={}", block.getHash());
            return false;
        }

        //step3 verify info
        List<Transaction> contractTransactionList = new LinkedList<>();
        RepositoryRoot blockRepository = new RepositoryRoot(contractRepository, block.getPrevBlockHash(), utxoServiceProxy, SystemProperties.getDefault());
        Money blockFee = new Money(0);
        String stateHash = StringUtils.EMPTY;
        for (int index = 1; index < txNumber; index++) {
            Transaction transaction = transactions.get(index);
            if (contractTransactionList.size() > 0) {
                Transaction contractTransaction = contractTransactionList.remove(0);
                if (StringUtils.equals(contractTransaction.getHash(), transaction.getHash())) {
                    continue;
                }
                LOGGER.warn("transaction and contract hash not equals txHash:{},contractHash:{}",
                        contractTransaction.getHash(), transaction.getHash());
                return false;
            }
            //step2 verify tx business info
            try {
                Map<String, Object> validResult = verifyTransactionForVoting(transaction, block, contractTransactionList, blockRepository);
                Money fee = (Money) validResult.get("fee");
                blockFee = blockFee.add(fee);
                Object contractStateHash = validResult.get("contractStateHash");
                if (contractStateHash != null) {
                    HashFunction function = Hashing.sha256();
                    stateHash = function.hashString(String.join(stateHash, contractStateHash.toString()), Charsets.UTF_8).toString();
                }
            } catch (Exception e) {
                LOGGER.error("verifyTransactionForVoting failed ex:{}", e);
                return false;
            }
        }
        String dbStateHash = blockRepository.getStateHash();
        if (org.apache.commons.lang.StringUtils.isNotEmpty(stateHash) && org.apache.commons.lang.StringUtils.isNotEmpty(dbStateHash)) {
            stateHash = contractService.appendStorageHash(stateHash, dbStateHash);
        }
        Money transactionsFee = block.getTransactionsFee();
        LOGGER.info("the blockFee is {} with block hash {}", blockFee, block.getHash());
        if (!transactionsFee.equals(blockFee)) {
            LOGGER.info("the transactionsFee is not wright {}", block.getHash());
            return false;
        }
        String contractStateHash = block.getContractStateHash();
        contractStateHash = contractStateHash == null ? StringUtils.EMPTY : contractStateHash;
        LOGGER.info("the stateHash is {} with block hash {}", stateHash, block.getHash());
        if (!StringUtils.equals(contractStateHash, stateHash)) {
            LOGGER.info("the contractStateHash is not wright {}", block.getHash());
            return false;
        }
        boolean verifyCoinBaseTx = verifyCoinBaseTx(transactions.get(0), block);
        if (!verifyCoinBaseTx) {
            return false;
        }

        LOGGER.info("check the transactions success of block {}", block.getHeight());
        return true;
    }

    @Override
    public void receivedTransaction(Transaction tx) {
        String hash = tx.getHash();
        LOGGER.info("receive a new transaction from remote with hash={}", hash);

        boolean isExist = txCacheManager.isContains(hash);
        if (isExist) {
            LOGGER.info("the transaction is exist in cache with hash {}", hash);
            return;
        }
        boolean valid = verifyTransaction(tx, null);
        if (!valid) {
            LOGGER.info("the transaction is not valid hash={}", hash);
            return;
        }

        txCacheManager.addTransaction(tx);
        broadcastTransaction(tx);
    }

    @Override
    public boolean hasStakeOnBest(String address, SystemCurrencyEnum currency) {
        Money balanceMoney = balanceService.getBalanceOnBest(address, currency.getCurrency());
        return getBalanceCurrency(balanceMoney, currency);
    }

    @Override
    public boolean hasStake(String preBlockHash, String address, SystemCurrencyEnum currency) {
        Money balanceMoney = balanceService.getUnionBalance(preBlockHash, address, currency.getCurrency());
        return getBalanceCurrency(balanceMoney, currency);
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

            String txHash = prevOutPoint.getTransactionHash();
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
            if (!hasStakeOnBest(address, SystemCurrencyEnum.MINER)) {
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
            if (hasStakeOnBest(address, SystemCurrencyEnum.MINER)) {
                result.add(address);
            }
        }
        return result;
    }


    public boolean getBalanceCurrency(Money balanceMoney, SystemCurrencyEnum currency) {
        Money stakeMinMoney = new Money("1", currency.getCurrency());
        return balanceMoney.compareTo(stakeMinMoney) >= 0;
    }

    public Map<String, Object> verifyTransactionForVoting(Transaction tx, Block
            block, List<Transaction> contractTransactionList, Repository blockRepository) {
        if (null == tx) {
            LOGGER.info("transaction is null");
            throw new RuntimeException();
        }
        if (!tx.valid()) {
            LOGGER.info("transaction is valid error");
            throw new RuntimeException();
        }
        Map<String, Object> validResult = new HashedMap();
        List<TransactionInput> inputs = tx.getInputs();
        List<TransactionOutput> outputs = tx.getOutputs();
        String hash = tx.getHash();

        String blockHash = block != null ? block.getHash() : null;
        String preBlockHash = block != null ? block.getPrevBlockHash() : null;
        Map<String, Money> preMoneyMap = new HashMap<>(8);
        HashSet<String> prevOutKey = new HashSet<>();
        for (TransactionInput input : inputs) {
            String key = input.getPrevOut().getKey();
            boolean notContains = prevOutKey.add(key);
            if (!notContains) {
                LOGGER.info("the input has been spend in this transaction or in the other transaction in the block,tx hash {}, the block hash {}"
                        , tx.getHash()
                        , blockHash);
                throw new RuntimeException();
            }
            TransactionOutput preOutput = getPreOutput(preBlockHash, input);
            if (preOutput == null) {
                LOGGER.info("pre-output is empty,input={},tx txHash={},block hash={}", input, tx.getHash(), blockHash);
                throw new RuntimeException();
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
            String currency = output.getMoney().getCurrency();
            if (!curMoneyMap.containsKey(currency)) {
                curMoneyMap.put(currency, new Money("0", currency).add(output.getMoney()));
            } else {
                curMoneyMap.put(currency, curMoneyMap.get(currency).add(output.getMoney()));
            }
        }

        Money surplus = new Money(0);
        boolean hasCas = false;
        BigInteger gas = tx.getGasPrice().multiply(BigInteger.valueOf(tx.getGasLimit()));
        Money gasToMoney = BalanceUtil.convertGasToMoney(gas, SystemCurrencyEnum.CAS.getCurrency());
        for (String key : curMoneyMap.keySet()) {
            Money preMoney = preMoneyMap.get(key);
            Money curMoney = curMoneyMap.get(key);

            if (preMoney == null) {
                LOGGER.info("Pre-output currency is null {}", key);
                throw new RuntimeException();
            }
            LOGGER.debug("input money :{}, output money:{}", preMoney.getValue(), curMoney.getValue());

            //if verify receivedTransaction, block is null so curMoney add tx fee
            if (StringUtils.equals(SystemCurrencyEnum.CAS.getCurrency(), key)) {
                //input >= out + gas*gasLimit
                curMoney.add(gasToMoney);
                hasCas = true;
            }
            if (preMoney.compareTo(curMoney) < 0) {
                LOGGER.info("Not enough fees, currency type:{}", key);
                throw new RuntimeException();
            }
            if (StringUtils.equals(SystemCurrencyEnum.CAS.getCurrency(), key)) {
                surplus = preMoney.subtract(curMoney);
            }
        }

        if (!hasCas) {
            LOGGER.info("there haven't input which the currency is cas,blockHash:", block.getHash());
            throw new RuntimeException();
        }

        boolean verifyInputs = verifyInputs(inputs, hash, preBlockHash);
        if (!verifyInputs) {
            throw new RuntimeException();
        }
        BigInteger totalGas;
        Money fee = new Money(0);
        if (tx.contractTrasaction()) {
            ContractService.InvokePO invokeResult = contractService.invoke(block, tx, blockRepository);
            ExecutionResult executionResult = invokeResult.getExecutionResult();
            Transaction contractTransaction = invokeResult.getContractTransaction();
            if (contractTransaction != null) {
                contractTransactionList.add(contractTransaction);
                if (StringUtils.isEmpty(executionResult.getErrorMessage())) {
                    Money contractTransactionFee = BalanceUtil.convertGasToMoney(
                            FeeUtil.getSizeGas(contractTransaction.size())
                                    .multiply(tx.getGasPrice()), SystemCurrencyEnum.CAS.getCurrency());
                    fee.add(contractTransactionFee);
                }
            }
            BigInteger payGas = BigInteger.valueOf(tx.getGasLimit()).subtract(executionResult.getRemainGas());
            totalGas = payGas.multiply(tx.getGasPrice());
            String contractStateHash = blockService.calculateExecutionHash(executionResult);
            validResult.put("contractStateHash", contractStateHash);
        } else {
            totalGas = BigInteger.valueOf(tx.getGasLimit()).multiply(tx.getGasPrice());
        }
        fee.add(BalanceUtil.convertGasToMoney(totalGas, SystemCurrencyEnum.CAS.getCurrency()));
        fee = fee.add(surplus);
        validResult.put("fee", fee);

        return validResult;
    }

    /**
     * validate tx
     *
     * @param tx    one tx
     * @param block current block
     * @return return result
     */

    public boolean verifyTransaction(Transaction tx, Block block) {
        if (null == tx) {
            LOGGER.info("transaction is null");
            return false;
        }
        if (block != null) {
            if (!tx.valid()) {
                LOGGER.info("transaction is valid error");
                return false;
            }
            if (!tx.sizeAllowed()) {
                LOGGER.info("Size of the transaction is illegal: {}", tx.getHash());
                return false;
            }
            if (!tx.validContractPart()) {
                LOGGER.info("Contract format is incorrect: {}", tx.getHash());
                return false;
            }
            if (!limitCheck(tx)) {
                LOGGER.info("size or gas limit is incorrect: {}", tx.getHash());
                return false;
            }
        }
        List<TransactionInput> inputs = tx.getInputs();
        List<TransactionOutput> outputs = tx.getOutputs();
        String hash = tx.getHash();

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

        // for contract, currency must be cas or of ico.
        if (tx.contractTrasaction()
                && !icoService.getContractCurrencies().contains(outputs.get(0).getMoney().getCurrency())) {
            return false;
        }

        boolean hasCas = false;
        for (String key : curMoneyMap.keySet()) {
            Money preMoney = preMoneyMap.get(key);
            Money curMoney = curMoneyMap.get(key);

            if (preMoney == null) {
                LOGGER.info("Pre-output currency is null {}", key);
                return false;
            }
            LOGGER.debug("input money :{}, output money:{}", preMoney.getValue(), curMoney.getValue());

            //if verify receivedTransaction, block is null so curMoney add tx fee
            if (StringUtils.equals(SystemCurrencyEnum.CAS.getCurrency(), key) && block == null) {
                //input >= out + gas*gasLimit
                BigInteger gas = tx.getGasPrice().multiply(BigInteger.valueOf(tx.getGasLimit()));
                curMoney.add(BalanceUtil.convertGasToMoney(gas, SystemCurrencyEnum.CAS.getCurrency()));
                hasCas = true;
            }
            if (preMoney.compareTo(curMoney) < 0) {
                LOGGER.info("Not enough fees, currency type:{}", key);
                return false;
            }
        }
        if (!hasCas) {
            LOGGER.info("there haven't input which the currency is cas,blockHash:", block.getHash());
            throw new RuntimeException();
        }

        return verifyInputs(inputs, hash, preBlockHash);
    }

    private boolean limitCheck(Transaction transaction) {
        long blockSizeLimit = blockchainConfig.getLimitedSize();
        long subTransactionSizeLimit = blockchainConfig.getContractLimitedSize();
        long blockGasLimit = blockchainConfig.getBlockGasLimit();


        long transactionSize = transaction.size();
        long transactionGasLimit = transaction.getGasLimit();


        if (transactionSize > blockSizeLimit) {
            return false;
        }

        // for contract transaction, subTransaction may be generated, with a size limitation.
        if (transaction.contractTrasaction() &&
                transactionSize + subTransactionSizeLimit > blockSizeLimit) {
            return false;
        }

        if (transactionGasLimit > blockGasLimit) {
            return false;
        }

        // gasLimit of transaction must be enough for sizeFee at least.
        if (transactionGasLimit < FeeUtil.getSizeGas(transactionSize).longValue()) {
            return false;
        }

        return true;
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
    public TransactionOutput getPreOutput(String preBlockHash, TransactionInput input) {
        String preOutKey = input.getPrevOut().getKey();
        if (StringUtils.isEmpty(preOutKey)) {
            LOGGER.info("preOutKey is empty,input={}", input.toJson());
            return null;
        }

        UTXO utxo;
        utxo = utxoServiceProxy.getUnionUTXO(preBlockHash, preOutKey);

        if (utxo == null) {
            LOGGER.info("UTXO is empty,input={},preOutKey={}", input.toJson(), preOutKey);
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
    public boolean verifyCoinBaseTx(Transaction tx, Block block) {
        if (!tx.inputsEmpty()) {
            LOGGER.info("Invalidate Coinbase transaction");
            return false;
        }
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

        Rewards rewards = transactionFeeService.countMinerAndWitnessRewards(block.getTransactionsFee(), block.getHeight());
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
        //verify witness coinbase output
        if (!validateWitnessOutput(outputs.subList(1, outputs.size()), rewards, block.getHeight())) {
            LOGGER.info("Validate witness reward failed");
            return false;
        }
        //verify reward count
        if (!validateRewards(outputs, rewards)) {
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
                LOGGER.debug("isEffective = true");
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
     * validate witness
     *
     * @param outputs witness reward  output
     * @param reward  total reward
     * @return return validate result
     */
    public boolean validateWitnessOutput(List<TransactionOutput> outputs, Rewards reward, Long height) {
        if (outputs.size() <= 0) {
            LOGGER.info("Witness coinbase transaction: UnLock script is null, outputs={},totalReward={}", outputs, reward.getTotalMoney());
            return false;
        }
        long lastReward = height % WITNESS_NUM;
        for (int i = 0; i < outputs.size(); i++) {
            LockScript script = outputs.get(i).getLockScript();
            if (script == null) {
                LOGGER.info("Witness coinbase transaction: Lock script is null, outputs={},totalReward={}", outputs, reward.getTotalMoney());
                return false;
            }

            if (!SystemCurrencyEnum.CAS.getCurrency().equals(outputs.get(i).getMoney().getCurrency())) {
                LOGGER.info("Invalid Witness coinbase transaction: Currency is not cas, outputs={},totalReward={}", outputs, reward.getTotalMoney());
                return false;
            }

            if (!reward.check()) {
                LOGGER.debug("Witness reward validate error");
                return false;
            }

            if (lastReward == i) {
                return outputs.get(i).getMoney().compareTo(reward.getLastWitnessMoney()) == 0;
            } else {
                return outputs.get(i).getMoney().compareTo(reward.getTopTenSingleWitnessMoney()) == 0;
            }
        }
        return false;
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
            LOGGER.debug("Producer coinbase transaction: totalReward is error,totalReward={}", totalReward);
            return false;
        }

        return output.getMoney().compareTo(totalReward) == 0;
    }


    /**
     * validate witness rewards
     *
     * @param outputs witness reward  outputs
     * @param rewards
     * @return if count outputs money == （topTenSingleWitnessMoney*10+lastWitnessMoney） return true else false
     */
    private boolean validateRewards(List<TransactionOutput> outputs, Rewards rewards) {
        Money outputsTotalMoney = new Money("0");
        outputs.forEach(output -> {
            outputsTotalMoney.add(output.getMoney());
        });
        Money minerAndWitnessTotalMoney = new Money("0");
        Money countWitnessMoney = new Money(rewards.getTopTenSingleWitnessMoney().getValue()).multiply(witnessService.getWitnessSize() - 1).add(rewards.getLastWitnessMoney());
        minerAndWitnessTotalMoney = countWitnessMoney.add(rewards.getMinerTotal());

        return minerAndWitnessTotalMoney.compareTo(outputsTotalMoney) == 0;
    }

    /**
     * received transaction if validate success and board
     *
     * @param tx received tx
     */
    public void broadcastTransaction(Transaction tx) {
        messageCenter.broadcast(tx);
        LOGGER.info("broadcast transaction hash={}", tx.getHash());
    }

    /**
     * calculation ordinary transaction fee
     *
     * @param tx Ordinary Transaction
     * @return cas fee
     */
    @Override
    public Money calculationOrdinaryTransactionFee(Transaction tx) {

        if (tx.contractTrasaction()) {
            throw new RuntimeException("tx is Contract Transaction");
        }

        return initialTransactionFee(tx).add(gasFee(tx));
    }

    public Money initialTransactionFee(Transaction transaction) {
        final HashMap<String, Money> feeMap = new HashMap<>(1);
        transaction.getInputs().stream().filter(ti -> ti.getPrevOut().getMoney().getCurrency().
                equals(SystemCurrencyEnum.CAS.getCurrency())).forEach(ti -> {
            Money txFee = feeMap.getOrDefault("txFee", new Money());
            txFee = txFee.add(ti.getPrevOut().getMoney());
            feeMap.put("txFee", txFee);
        });

        transaction.getOutputs().stream().filter(ti -> ti.getMoney().getCurrency().
                equals(SystemCurrencyEnum.CAS.getCurrency())).forEach(ti -> {
            Money txFee = feeMap.getOrDefault("txFee", new Money());
            txFee = txFee.subtract(ti.getMoney());
            feeMap.put("txFee", txFee);
        });

        Money txFee = feeMap.getOrDefault("txFee", new Money());
        txFee = txFee.subtract(gasFee(transaction));
        feeMap.put("txFee", txFee);

        return feeMap.get("txFee");
    }

    public Money gasFee(Transaction transaction) {
        return BalanceUtil.convertGasToMoney(BigInteger.valueOf(transaction.getGasLimit())
                        .multiply(transaction.getGasPrice())
                , SystemCurrencyEnum.CAS.getCurrency());
    }
}