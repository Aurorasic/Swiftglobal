package cn.primeledger.cas.global.blockchain.transaction;

import cn.primeledger.cas.global.blockchain.Block;
import cn.primeledger.cas.global.blockchain.BlockIndex;
import cn.primeledger.cas.global.blockchain.BlockService;
import cn.primeledger.cas.global.blockchain.BlockWitness;
import cn.primeledger.cas.global.blockchain.listener.MessageCenter;
import cn.primeledger.cas.global.crypto.ECKey;
import cn.primeledger.cas.global.crypto.model.KeyPair;
import cn.primeledger.cas.global.script.LockScript;
import cn.primeledger.cas.global.script.UnLockScript;
import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.ConcurrentMap;
import java.util.stream.Collectors;

/**
 * @author baizhengwen
 * @date 2018/2/24
 */
@Service
@Slf4j
public class TransactionService {
    private final static BigDecimal MINER_AMOUNT = new BigDecimal("1.0");
    private static final int MAX_WITNESS_NUM = 3;
    private static final int MIN_OUTPUT_SIZE = 1;
    public static BigDecimal MINER_STAKE_MIX_AMOUNT = BigDecimal.ONE;
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

        if (MIN_OUTPUT_SIZE > outputs.size()) {
            LOGGER.error("outputs number is less than one,tx hash={}_block hash={}", tx.getHash(), block.getHash());
            return false;
        }

        //todo kongyu 2018-3-30
        Block preBlock = blockService.getBlock(block.getPrevBlockHash());
        if (preBlock == null) {
            LOGGER.error("preBlock == null,tx hash={}_block hash={}", tx.getHash(), block.getHash());
            return false;
        }
        if (!validPreBlock(preBlock, block.getHeight())) {
            LOGGER.error("pre block is not last best block,tx hash={}_block hash={}", tx.getHash(), block.getHash());
            return false;
        }

        BigDecimal totalReward = getTotalTransactionsRewards(preBlock);

        //verify miner coinbase output
        if (!validateProducerOutput(outputs.get(0), preBlock, totalReward)) {
            LOGGER.error("verify miner coinbase output failed,tx hash={}_block hash={}", tx.getHash(), block.getHash());
            return false;
        }

        //verify witness coinbase outputs
        int size = outputs.size();
        if (MIN_OUTPUT_SIZE < size) {
            return validateWitnessOutputs(outputs.subList(1, size), preBlock, totalReward);
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

    public boolean validateProducerOutput(TransactionOutput output, Block preBlock, BigDecimal totalReward) {
        if (null == output || null == preBlock) {
            LOGGER.error("Producer coinbase transaction: Lock script is null, output={}_totalReward={}", output, totalReward);
            return false;
        }

        if (StringUtils.isEmpty(output.getCurrency())) {
            LOGGER.error("Invalid producer coinbase transaction: Currency is null, output={}_preBlock hash={}_totalReward={}", output, preBlock.getHash(), totalReward);
            return false;
        }

        LockScript script = output.getLockScript();
        if (script == null) {
            LOGGER.error("Producer coinbase transaction: Lock script is null, output={}_preBlock hash={}_totalReward={}", output, preBlock.getHash(), totalReward);
            return false;
        }

        BlockWitness signature = preBlock.getMinerFirstPKSig();
        String preBlockProducerAddr = signature.getAddress();
        String curProducerAddr = script.getAddress();

        if (!preBlockProducerAddr.equals(curProducerAddr)) {
            LOGGER.error("Invalid producer coinbase transaction: Address not match, output={}_preBlock hash={}_totalReward={}", output, preBlock.getHash(), totalReward);
            return false;
        }

        if (!SystemCurrencyEnum.CAS.getCurrency().equals(output.getCurrency())) {
            LOGGER.error("Invalid producer coinbase transaction: Currency is not cas, output={}_preBlock hash={}_totalReward={}", output, preBlock.getHash(), totalReward);
            return false;
        }

        if (!validateProducerReward(output, preBlock, totalReward)) {
            LOGGER.error("Validate producer reward failed, output={}_preBlock hash={}_totalReward={}", output, preBlock.getHash(), totalReward);
            return false;
        }

        return true;
    }


    private boolean validateProducerReward(TransactionOutput output, Block preBlock, BigDecimal totalReward) {
        if (totalReward == null) {
            LOGGER.error("Pre-block all transactions' reward is null, output={}_preBlock hash={}_totalReward={}", output, preBlock.getHash(), totalReward);
            return false;
        }

        List<BlockWitness> signatures = preBlock.getOtherWitnessSigPKS();
        if (CollectionUtils.isEmpty(signatures)) {
            BigDecimal preReward = totalReward.multiply(new BigDecimal(0.1));
            if (preReward.compareTo(output.getAmount()) != 0) {
                LOGGER.error("Producer reward is invalid, output={}_preBlock hash={}_totalReward={}", output, preBlock.getHash(), totalReward);
                return false;
            }
        } else {
            BigDecimal preReward = totalReward.multiply(new BigDecimal(0.5));
            if (preReward.compareTo(output.getAmount()) != 0) {
                LOGGER.error("Producer reward is invalid, output={}_preBlock hash={}_totalReward={}", output, preBlock.getHash(), totalReward);
                return false;
            }
        }

        return true;
    }

    public boolean validateWitnessOutputs(List<TransactionOutput> outputs, Block preBlock, BigDecimal totalReward) {
        if (null == outputs || null == preBlock) {
            LOGGER.error("outputs or preBlock is null");
            return false;
        }

        if (CollectionUtils.isEmpty(outputs)) {
            LOGGER.error("Witness coinbase transaction: Outputs is empty, outputs={}_preBlock hash={}_totalReward={}", outputs, preBlock.getHash(), totalReward);
            return false;
        }

        List<BlockWitness> signatures = preBlock.getOtherWitnessSigPKS();
        if (CollectionUtils.isEmpty(signatures) || signatures.size() > MAX_WITNESS_NUM) {
            LOGGER.error("Witness coinbase transaction: Pre-signatures are invalid, outputs={}_preBlock hash={}_totalReward={}", outputs, preBlock.getHash(), totalReward);
            return false;
        }

        if (outputs.size() != signatures.size()) {
            LOGGER.error("Witness coinbase transaction: Signatures not match, outputs={}_preBlock hash={}_totalReward={}", outputs, preBlock.getHash(), totalReward);
            return false;
        }

        int size = signatures.size();
        for (int i = 0; i < size; i++) {
            BlockWitness signature = signatures.get(i);
            String preAddr = signature.getAddress();
            TransactionOutput output = outputs.get(i);
            LockScript script = output.getLockScript();
            if (script == null) {
                LOGGER.error("Witness coinbase transaction: Lock script is null, outputs={}_preBlock hash={}_totalReward={}", outputs, preBlock.getHash(), totalReward);
                return false;
            }

            if (!preAddr.equals(script.getAddress())) {
                LOGGER.error("Invalid Witness coinbase transaction: Address not match, outputs={}_preBlock hash={}_totalReward={}", outputs, preBlock.getHash(), totalReward);
                return false;
            }

            if (StringUtils.isEmpty(output.getCurrency())) {
                LOGGER.error("Invalid Witness coinbase transaction: Currency is null, outputs={}_preBlock hash={}_totalReward={}", outputs, preBlock.getHash(), totalReward);
                return false;
            }

            if (!SystemCurrencyEnum.CAS.getCurrency().equals(output.getCurrency())) {
                LOGGER.error("Invalid Witness coinbase transaction: Currency is not cas, outputs={}_preBlock hash={}_totalReward={}", outputs, preBlock.getHash(), totalReward);
                return false;
            }
        }

        if (!validateWitnessRewards(outputs, preBlock, totalReward)) {
            LOGGER.error("Validate witness reward failed");
            return false;
        }

        return true;
    }

    private boolean validateWitnessRewards(List<TransactionOutput> outputs, Block preBlock, BigDecimal totalReward) {
        if (totalReward == null) {
            LOGGER.error("Pre-block all transactions' reward is null");
            return false;
        }

        List<BlockWitness> signatures = preBlock.getOtherWitnessSigPKS();

        int signatureNum = signatures.size();
        BigDecimal preReward = totalReward.multiply(new BigDecimal(0.5));
        BigDecimal witnessReward = preReward.divide(new BigDecimal(signatureNum), 8, BigDecimal.ROUND_DOWN);

        outputs.stream().forEach(output -> {
            if (witnessReward.compareTo(output.getAmount()) != 0) {
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
        boolean checkInput = block == null || !block.isgenesisBlock();
        /*if (checkInput && CollectionUtils.isEmpty(inputs)) {
            LOGGER.error("inputs is empty");
            return false;
        }
        if (CollectionUtils.isEmpty(outputs)) {
            LOGGER.error("outputs is empty");
            return false;
        }*/
        if (!tx.sizeAllowed()) {
            LOGGER.error("Size of the transaction is illegal.");
            return false;
        }

        Map<String, BigDecimal> preAmountMap = new HashMap<>();
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
                        , block.getHash());
                return false;
            }
            TransactionOutput preOutput = getPreOutput(input);
            if (preOutput == null) {
                LOGGER.error("pre-output is empty,input={}_preOutput={}_tx hash={}_block hash={}", input, preOutput, tx.getHash(), block.getHash());
                return false;
            }

            BigDecimal preAmount = preAmountMap.get(preOutput.getCurrency());
            if (preAmount == null) {
                preAmountMap.put(preOutput.getCurrency(), preOutput.getAmount());
            } else {
                preAmount = preAmount.add(preOutput.getAmount());
                preAmountMap.put(preOutput.getCurrency(), preAmount);
            }
        }

        Map<String, BigDecimal> curAmountMap = new HashMap<>();
        for (TransactionOutput output : outputs) {
            if (!output.valid()) {
                LOGGER.error("Current output is invalid");
                return false;
            }

            BigDecimal curAmount = curAmountMap.get(output.getCurrency());
            if (curAmount == null) {
                curAmountMap.put(output.getCurrency(), output.getAmount());
            } else {
                curAmount = curAmount.add(output.getAmount());
                curAmountMap.put(output.getCurrency(), curAmount);
            }
        }

        if (preAmountMap.keySet().size() != curAmountMap.keySet().size()) {
            LOGGER.error("Pre-output currency type different from current");
            return false;
        }

        for (String key : curAmountMap.keySet()) {
            BigDecimal preAmount = preAmountMap.get(key);
            BigDecimal curAmount = curAmountMap.get(key);

            if (preAmount == null) {
                LOGGER.error("Pre-output currency is null {}", key);
                return false;
            }

            if (curAmount == null) {
                LOGGER.error("Current output currency is null {}", key);
                return false;
            }

            if (org.apache.commons.lang.StringUtils.equals(SystemCurrencyEnum.CAS.getCurrency(), key)) {
                curAmount = curAmount.add(getCurrencyFee(""));
                if (preAmount.compareTo(curAmount) < 0) {
                    LOGGER.error("Not enough cas fees");
                    return false;
                }
            } else {
                if (preAmount.compareTo(curAmount) < 0) {
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
    private BigDecimal getCurrencyFee(String currency) {
        return new BigDecimal("0.001");
    }

    private boolean verifyInputs(List<TransactionInput> inputs, String hash) {
        int size = inputs.size();
        TransactionInput input = null;
        UnLockScript unLockScript = null;
        for (int i = 0; i < size; i++) {
            input = inputs.get(i);
            if (null == input) {
                LOGGER.error("the input is empty {}",i);
                return false;
            }
            unLockScript = input.getUnLockScript();
            if (null == unLockScript) {
                LOGGER.error("the unLockScript is empty {}",i);
                return false;
            }
            TransactionIndex preTxIndex = transactionIndexData.get(input.getPrevOut().getHash());
            if (preTxIndex == null) {
                //if the transactionIndex is not exist,
                //so local data is not right
                LOGGER.error("the preTxIndex is empty {}",i);
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
                    LOGGER.error("the block is empty {}",i);
                    return false;
                }
                BlockIndex blockIndex = blockIndexData.get(block.getHeight());
                if (blockIndex == null) {
                    LOGGER.error("the blockIndex is empty {}",i);
                    //if the blockIndex is not exist,
                    //so local data is not right
                    return false;
                }
                boolean best = blockIndex.isBest(txHash);
                if (best) {
                    LOGGER.error("the blockIndex is empty {}",i);
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

    public Transaction buildMinerJoinTx(List<String> addrs, long lockTime, short version, BigDecimal fee) {
        if (org.apache.commons.collections.CollectionUtils.isEmpty(addrs)) {
            throw new RuntimeException("addrs is empty");
        }
        if (null == fee || fee.compareTo(BigDecimal.ZERO) < 0) {
            throw new RuntimeException("fee is null or more than less 0");
        }
        if (null == peerKeyPair) {
            throw new RuntimeException("peerKeyPair is null");
        }
        if (0 > lockTime) {
            throw new RuntimeException("lockTime is less 0");
        }
        if (0 > version) {
            throw new RuntimeException("version is less 0");
        }

        String address = ECKey.pubKey2Base58Address(peerKeyPair.getPubKey());
        //String address = ECKey.pubKey2Base58Address("02039f5ac93a0ae01b20ff35aeb2911d8fc95158d7f90f8c3a870dad1c1b63fb7c");
        Transaction minerTx = new Transaction();
        minerTx.setMinerPubKey(peerKeyPair.getPubKey());
        minerTx.setVersion(version);
        minerTx.setLockTime(lockTime);
        List<UTXO> utxos = utxoMap.values().stream().filter(utxo -> StringUtils.equals(address, utxo.getAddress())).collect(Collectors.toList());

        if (org.apache.commons.collections.CollectionUtils.isEmpty(utxos)) {
            return null;
        }
        //交易手续费用的UTXO
        UTXO feeUtxo = null;
        //挖矿权益的UTXO
        UTXO minerUtxo = null;

        for (UTXO utxo : utxos) {
            if (utxo.isCASCurrency()
                    && utxo.getOutput().getAmount().compareTo(fee) >= 0) {
                feeUtxo = utxo;
            }
            //一个矿工的挖矿权益根据MINER币的个数决定，只有当MINER币>=1时，才有权利挖矿
            if (utxo.isMinerCurrency()
                    && utxo.getOutput().getAmount().compareTo(new BigDecimal(addrs.size())) >= 0) {
                //手续费计算
                minerUtxo = utxo;
            }
        }
        if (null == feeUtxo) {
            throw new RuntimeException("the transfer fee is insufficient.");
        }
        if (null == minerUtxo) {
            throw new RuntimeException("Insufficient mining rights and interests.");
        }
        //剩余的交易手续费用金额
        BigDecimal feeAssets = feeUtxo.getOutput().getAmount();
        //剩余的挖矿权益金额
        BigDecimal minerAssets = minerUtxo.getOutput().getAmount();

        TransactionInput feeInput = biuldTransactionInput(feeUtxo);
        TransactionInput minerInput = biuldTransactionInput(minerUtxo);
        if (null == feeInput) {
            throw new RuntimeException("feeInput is null");
        }
        if (null == minerInput) {
            throw new RuntimeException("minerInput is null");
        }
        //input
        List<TransactionInput> inputLists = Lists.newArrayList();
        inputLists.add(feeInput);
        inputLists.add(minerInput);
        minerTx.setInputs(inputLists);

        //output
        List outputList = Lists.newArrayList();

        for (String addr : addrs) {
            if (!ECKey.checkBase58Addr(addr)) {
                continue;
            }
            TransactionOutput minerOutput = generateTransactionOutput(addr, SystemCurrencyEnum.MINER, MINER_AMOUNT);
            if (null != minerOutput) {
                minerAssets = minerAssets.subtract(MINER_AMOUNT);
                outputList.add(minerOutput);
            }
        }
        if (org.apache.commons.collections.CollectionUtils.isEmpty(outputList)) {
            return null;
        }
        //找MINER零钱
        TransactionOutput minerRest = generateTransactionOutput(address, SystemCurrencyEnum.MINER, minerAssets);
        if (null != minerRest) {
            outputList.add(minerRest);
        }
        //找CAS零钱
        TransactionOutput casRest = generateTransactionOutput(address, SystemCurrencyEnum.CAS, feeAssets.subtract(fee));
        if (null != casRest) {
            outputList.add(casRest);
        }
        minerTx.setOutputs(outputList);
        String signatrue = ECKey.signMessage(minerTx.getHash(), peerKeyPair.getPriKey());
        minerTx.getInputs().forEach(transactionInput -> {
            UnLockScript unLockScript = transactionInput.getUnLockScript();
            if (unLockScript != null) {
                unLockScript.getPkList().add(peerKeyPair.getPubKey());
                unLockScript.getSigList().add(signatrue);
            }
        });
        return minerTx;
    }

    public Transaction buildStopOrRestartMineTx(String otherAddress, long lockTime, short version, BigDecimal amount, BigDecimal fee) {
        if (!ECKey.checkBase58Addr(otherAddress)) {
            throw new RuntimeException("Incorrect address format.");
        }
        if (null == amount) {
            throw new RuntimeException("amount can not be null");
        }
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new RuntimeException("The amount is not between 0 and 1 ");
        }
        if (null == fee || fee.compareTo(BigDecimal.ZERO) < 0) {
            throw new RuntimeException("fee is null or more than less 0");
        }

        String address = ECKey.pubKey2Base58Address(peerKeyPair.getPubKey());
        Transaction minerTx = new Transaction();
        minerTx.setMinerPubKey(peerKeyPair.getPubKey());
        minerTx.setVersion(version);
        minerTx.setLockTime(lockTime);
        List<UTXO> utxos = utxoMap.values().stream().filter(utxo -> StringUtils.equals(address, utxo.getAddress())).collect(Collectors.toList());

        if (CollectionUtils.isEmpty(utxos)) {
            return null;
        }
        //交易手续费用
        UTXO feeUtxo = null;
        //挖矿权益输入
        UTXO minerUtxo = null;

        for (UTXO utxo : utxos) {
            if (utxo.isCASCurrency()
                    && utxo.getOutput().getAmount().compareTo(fee) >= 0) {
                feeUtxo = utxo;
            }
            if (utxo.isMinerCurrency()
                    && utxo.hasMinerStake()) {
                minerUtxo = utxo;
            }
        }
        if (null == feeUtxo) {
            throw new RuntimeException("the transfer fee is insufficient.");
        }
        if (null == minerUtxo) {
            throw new RuntimeException("Insufficient mining rights and interests.");
        }
        //剩余的交易手续费用金额
        BigDecimal feeAssets = feeUtxo.getOutput().getAmount();
        //剩余的挖矿权益金额
        BigDecimal minerAssets = minerUtxo.getOutput().getAmount();

        TransactionInput feeInput = biuldTransactionInput(feeUtxo);
        TransactionInput minerInput = biuldTransactionInput(minerUtxo);
        if (null == feeInput) {
            throw new RuntimeException("feeInput is null");
        }
        if (null == minerInput) {
            throw new RuntimeException("minerInput is null");
        }
        List<TransactionInput> inputs = Lists.newArrayList();
        inputs.add(feeInput);
        inputs.add(minerInput);
        minerTx.setInputs(inputs);

        //output
        List outputList = Lists.newArrayList();
        //将一部分MINER转到另一个自己的地址上
        TransactionOutput minerOutput2OtherAddress = generateTransactionOutput(otherAddress, SystemCurrencyEnum.MINER, amount);
        if (minerOutput2OtherAddress != null) {
            outputList.add(minerOutput2OtherAddress);
        }
        //MINER找零
        minerAssets = minerAssets.subtract(amount);
        if (minerAssets.compareTo(BigDecimal.ZERO) > 0) {
            TransactionOutput minerOutput2Address = generateTransactionOutput(address, SystemCurrencyEnum.MINER, minerAssets);
            if (minerOutput2Address != null) {
                outputList.add(minerOutput2Address);
            }
        }

        if (CollectionUtils.isEmpty(outputList)) {
            return null;
        }

        //找CAS零钱
        TransactionOutput casRest = generateTransactionOutput(address, SystemCurrencyEnum.CAS, feeAssets.subtract(fee));
        if (null != casRest) {
            outputList.add(casRest);
        }
        minerTx.setOutputs(outputList);
        String signMessage = ECKey.signMessage(minerTx.getHash(), peerKeyPair.getPriKey());
        inputs.forEach(transactionInput -> {
            UnLockScript unLockScript = transactionInput.getUnLockScript();
            if (unLockScript != null) {
                unLockScript.getPkList().add(peerKeyPair.getPubKey());
                unLockScript.getSigList().add(signMessage);
            }
        });
        return minerTx;
    }

    private TransactionInput biuldTransactionInput(UTXO utxo) {
        if (null == utxo) {
            return null;
        }
        TransactionInput intput = new TransactionInput();
        TransactionOutPoint feeOutPoint = new TransactionOutPoint();
        UnLockScript feeUnLockScript = new UnLockScript();
        feeUnLockScript.setPkList(Lists.newArrayList());
        feeUnLockScript.setSigList(Lists.newArrayList());
        feeOutPoint.setHash(utxo.getHash());
        feeOutPoint.setIndex(utxo.getIndex());
        intput.setPrevOut(feeOutPoint);
        intput.setUnLockScript(feeUnLockScript);
        return intput;
    }

    private TransactionOutput generateTransactionOutput(String address, SystemCurrencyEnum currencyEnum, BigDecimal amount) {
        if (!ECKey.checkBase58Addr(address)) {
            return null;
        }
        if (null == currencyEnum) {
            return null;
        }
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            return null;
        }
        TransactionOutput output = new TransactionOutput();
        output.setAmount(amount);
        output.setCurrency(currencyEnum.getCurrency());
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

        BigDecimal totalRewards = getTotalTransactionsRewards(block);

        Transaction transaction = new Transaction();
        transaction.setMinerPubKey(peerKeyPair.getPubKey());
        transaction.setVersion(version);
        transaction.setLockTime(lockTime);

        List outputList = Lists.newArrayList();
        List<BlockWitness> witnessList = block.getOtherWitnessSigPKS();

        if (CollectionUtils.isEmpty(witnessList)) {
            //producer occupy 10%
            BigDecimal rewards = totalRewards.multiply(new BigDecimal(0.1));
            TransactionOutput output = genProducerCoinbaseOutput(minerPKSig.getAddress(), rewards);
            outputList.add(output);
        } else {
            //producer occupy 50%，others for witness
            BigDecimal rewards = totalRewards.multiply(new BigDecimal(0.5));
            String producerAddr = minerPKSig.getAddress();
            TransactionOutput produerCoinbaseOutput = genProducerCoinbaseOutput(producerAddr, rewards);
            List<TransactionOutput> witnessCoinbaseOutput = genWitnessCoinbaseOutput(block, rewards);

            outputList.add(produerCoinbaseOutput);
            outputList.addAll(witnessCoinbaseOutput);
        }

        transaction.setOutputs(outputList);

        return transaction;
    }

    private TransactionOutput genProducerCoinbaseOutput(String address, BigDecimal rewards) {
        TransactionOutput transactionOutput = new TransactionOutput();
        transactionOutput.setAmount(rewards);
        LockScript lockScript = new LockScript();
        lockScript.setAddress(address);
        transactionOutput.setLockScript(lockScript);
        transactionOutput.setCurrency(SystemCurrencyEnum.CAS.getCurrency());

        return transactionOutput;
    }

//    private Transaction buildProducerCoinBaseTx(String address, BigDecimal producerReward, long lockTime, short version) {
//        Transaction transaction = new Transaction();
//        transaction.setVersion(version);
//        transaction.setLockTime(lockTime);
//
//        TransactionOutput transactionOutput = new TransactionOutput();
//        transactionOutput.setAmount(producerReward);
//        LockScript lockScript = new LockScript();
//        lockScript.setAddress(address);
//        transactionOutput.setLockScript(lockScript);
//        List emptyList = Lists.newArrayList();
//        emptyList.add(transactionOutput);
//        transaction.setOutputs(emptyList);
//        return transaction;
//    }

    private BigDecimal getTotalTransactionsRewards(Block block) {
        BigDecimal totalFee = new BigDecimal("1");

//        List<Transaction> transactions = block.getTransactions();
//        for (Transaction transaction : transactions) {
//            totalFee = totalFee.add(getOneTransactionFee(transaction));
//        }


        LOGGER.info("Transactions' total fee : {}", totalFee);

        //BigDecimal percent = new BigDecimal("0.4"); //producer occupy rewards 40%
        //return totalFee.multiply(percent);

        return totalFee;
    }

    private BigDecimal getOneTransactionFee(Transaction transaction) {
        List<TransactionInput> inputs = transaction.getInputs();
        List<TransactionOutput> outputs = transaction.getOutputs();

        BigDecimal preOutAmount = new BigDecimal("0");
        for (TransactionInput input : inputs) {
            String preOutKey = input.getPrevOut().getKey();
            UTXO utxo = utxoMap.get(preOutKey);
            TransactionOutput output = utxo.getOutput();
            if (output.isCASCurrency()) {
                preOutAmount = preOutAmount.add(output.getAmount());
            }
        }

        LOGGER.info("Transactions' pre-output amount : {}", preOutAmount);

        BigDecimal outPutAmount = new BigDecimal("0");
        for (TransactionOutput output : outputs) {
            if (output.isCASCurrency()) {
                outPutAmount = outPutAmount.add(output.getAmount());
            }
        }

        LOGGER.info("Transactions' output amount : {}", outPutAmount);

        return preOutAmount.subtract(outPutAmount);
    }

//    private Transaction buildWitnessCoinBaseTx(List<TransactionOutput> outputList, long lockTime, short version) {
//        Transaction transaction = new Transaction();
//        transaction.setVersion(version);
//        transaction.setLockTime(lockTime);
//        transaction.setOutputs(outputList);
//        return transaction;
//    }

    private List<TransactionOutput> genWitnessCoinbaseOutput(Block block, BigDecimal producerReward) {
        List<BlockWitness> otherPKSigs = block.getOtherWitnessSigPKS();
        List<TransactionOutput> outputList = Lists.newArrayList();

        int size = otherPKSigs.size();
        otherPKSigs.forEach(signature -> {
            String address = signature.getAddress();
            BigDecimal witnessReword = producerReward.divide(new BigDecimal(size), 8, BigDecimal.ROUND_DOWN);

            TransactionOutput transactionOutput = new TransactionOutput();
            transactionOutput.setAmount(witnessReword);
            LockScript lockScript = new LockScript();
            lockScript.setAddress(address);
            transactionOutput.setLockScript(lockScript);
            transactionOutput.setCurrency(SystemCurrencyEnum.CAS.getCurrency());
            outputList.add(transactionOutput);
        });


        return outputList;
    }

    //for test when there haven't wallet
//    public Transaction buildTransfer(BigDecimal amount, String address, long lockTime, short version, String extra) {
//        if (amount == null) {
//            throw new RuntimeException("please set the amount");
//        }
//        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
//            throw new RuntimeException("the amount need more than zero");
//        }
//        ECKey casKey = ECKey.fromPrivateKey(peerKeyPair);
//        Transaction tx = new Transaction();
//        tx.setVersion(version);
//        if (StringUtils.isBlank(extra)) {
//            tx.setType(TransactionTypeEnum.TRANSFER.getType());
//        } else {
//            tx.setType(TransactionTypeEnum.TRANSFER_EXTRA.getType());
//        }
//        tx.setExtra(extra);
//        tx.setLockTime(lockTime);
//        Iterator<String> stringIterator = utxoData.keySet().iterator();
//
//        BigDecimal amt = amount;
//        List<String> spendUTXOKey = new ArrayList<>();
//        List<TransactionOutput> newOutpt = new ArrayList<>();
//        List<TransactionInput> newInput = new ArrayList<>();
//        tx.setInputs(newInput);
//        tx.setOutputs(newOutpt);
//
//        TransactionOutput transactionOutput = new TransactionOutput();
//        transactionOutput.setAmount(amount);
//        LockScript lockScript = new LockScript();
//        lockScript.setAddress(address);
//        transactionOutput.setLockScript(lockScript);
//        newOutpt.add(transactionOutput);
//
//        while (stringIterator.hasNext()) {
//            String next = stringIterator.next();
//            UTXO utxo = utxoData.get(next);
//            if (!StringUtils.equals(utxo.getAddress(), casKey.toBase58Address())) {
//                continue;
//            }
//
//            spendUTXOKey.add(next);
//            BigDecimal tempAmount = utxo.getAmount();
//            transactionOutput.setCurrency(utxo.getCurrency());
//
//            TransactionInput transactionInput = new TransactionInput();
//            TransactionOutPoint transactionOutPoint = new TransactionOutPoint();
//            transactionOutPoint.setHash(utxo.getHash());
//            transactionOutPoint.setIndex(utxo.getIndex());
//            transactionInput.setPrevOut(transactionOutPoint);
//            UnLockScript unLockScript = new UnLockScript();
//            List<String> pubKeyList = Lists.newArrayList();
//            List<String> sigList = Lists.newArrayList();
//            unLockScript.setPkList(pubKeyList);
//            unLockScript.setSigList(sigList);
//            transactionInput.setUnLockScript(unLockScript);
//            newInput.add(transactionInput);
//
//
//            if (tempAmount.compareTo(amt) == 0) {
//                //don't have other output
//                amt = amt.subtract(tempAmount);
//                break;
//            } else if (tempAmount.compareTo(amt) == -1) {
//                amt = amt.subtract(tempAmount);
//                continue;
//            } else if (tempAmount.compareTo(amt) == 1) {
//                amt = amt.subtract(tempAmount);
//                TransactionOutput temp = new TransactionOutput();
//                temp.setAmount(amt.multiply(BigDecimal.valueOf(-1)));
//                temp.setCurrency(utxo.getCurrency());
//                LockScript ls = new LockScript();
//                ls.setAddress(casKey.toBase58Address());
//                temp.setLockScript(ls);
//                newOutpt.add(temp);
//
//                break;
//            }
//        }
//        if (amt.compareTo(BigDecimal.ZERO) > 0) {
//            return null;
////            throw new RuntimeException("can not find enough UTXO");
//        }
//        String hash = tx.getHash();
//        String signMessage = casKey.signMessage(hash);
//        newInput.forEach(transactionInput -> {
//            UnLockScript unLockScript = transactionInput.getUnLockScript();
//            if (unLockScript != null) {
//                unLockScript.getPkList().add(casKey.getKeyPair().getPubKey());
//                unLockScript.getSigList().add(signMessage);
//            }
//        });
//        boolean valid = valid(tx);
//        if (!valid) {
//            return null;
//        }
//        broadcastTransaction(tx);
//        spendUTXOKey.forEach((used) -> {
//            utxoData.remove(used);
//        });
//        return tx;
//    }


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

    public Set<String> removedMiners(Transaction tx) {
        Set<String> result = new HashSet<>();
        List<TransactionInput> inputs = tx.getInputs();
        if (CollectionUtils.isEmpty(inputs)) {
            return result;
        }
        for (TransactionInput input : inputs) {
            TransactionOutPoint prevOut = input.getPrevOut();

            String txHash = prevOut.getHash();
            TransactionIndex transactionIndex = transactionIndexData.get(txHash);
            String blockHash = transactionIndex.getBlockHash();
            Block block = blockService.getBlock(blockHash);
            Transaction transactionByHash = block.getTransactionByHash(txHash);
            short index = prevOut.getIndex();
            TransactionOutput transactionOutputByIndex = null;
            if (transactionByHash != null) {
                transactionOutputByIndex = transactionByHash.getTransactionOutputByIndex(index);
            }
            if (transactionOutputByIndex == null) {
                continue;
            }
            String address = transactionOutputByIndex.getLockScript().getAddress();
            if (result.contains(address)) {
                continue;
            }
            if (!hasMinerStake(address)) {
                result.add(address);
            }
        }
        return result;
    }

    public Set<String> addedMiners(Transaction tx) {
        Set<String> result = new HashSet<>();
        List<TransactionOutput> outputs = tx.getOutputs();
        for (int i = 0; i < outputs.size(); i++) {
            UTXO utxo = utxoMap.get(UTXO.buildKey(tx.getHash(), (short) i));
            if (utxo == null) {
                LOGGER.warn("cannot find utxo when addedMiners of txhash: " + tx.getHash());
                continue;
            }
            String address = utxo.getAddress();
            if (result.contains(address)) {
                continue;
            }
            if (utxo.isMinerCurrency() &&
                    hasMinerStake(address)) {
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
            if (StringUtils.equals(utxo.getOutput().getCurrency(), currency) &&
                    StringUtils.equals(address, utxo.getAddress())) {
                result.add(utxo);
            }
        }
        return result;
    }

    public boolean hasMinerStake(String address) {
        List<UTXO> result = getUTXOList(address, SystemCurrencyEnum.MINER.getCurrency());
        BigDecimal amount = BigDecimal.ZERO;
        for (UTXO utxo : result) {
            amount = amount.add(utxo.getOutput().getAmount());
        }
        if (amount.compareTo(MINER_STAKE_MIX_AMOUNT) >= 0) {
            return true;
        }
        return false;
    }

    public void broadcastTransaction(Transaction tx) {
        messageCenter.broadcast(tx);
        LOGGER.info("broadcast transaction success: " + tx.getHash());
    }

}
