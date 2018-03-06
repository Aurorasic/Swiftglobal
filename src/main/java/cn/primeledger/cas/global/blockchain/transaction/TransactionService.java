package cn.primeledger.cas.global.blockchain.transaction;

import cn.primeledger.cas.global.Application;
import cn.primeledger.cas.global.blockchain.Block;
import cn.primeledger.cas.global.blockchain.BlockIndex;
import cn.primeledger.cas.global.blockchain.BlockService;
import cn.primeledger.cas.global.blockchain.PubKeyAndSignaturePair;
import cn.primeledger.cas.global.common.entity.BroadcastMessageEntity;
import cn.primeledger.cas.global.common.event.BroadcastEvent;
import cn.primeledger.cas.global.constants.EntityType;
import cn.primeledger.cas.global.crypto.ECKey;
import cn.primeledger.cas.global.crypto.model.KeyPair;
import cn.primeledger.cas.global.script.LockScript;
import cn.primeledger.cas.global.script.UnLockScript;
import com.alibaba.fastjson.JSON;
import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentMap;

/**
 * @author baizhengwen
 * @date 2018/2/24
 */
@Service
@Slf4j
public class TransactionService {

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
    private ConcurrentMap<String, UTXO> utxoData;

    @Autowired
    private TransactionCacheManager txCacheManager;

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

    public boolean valid(BaseTx tx) {
        short version = tx.getVersion();
        short type = tx.getType();
        String extra = tx.getExtra();
        if (version < 0) {
            return false;
        }
        if (!TransactionTypeEnum.containType(type)) {
            return false;
        }

        if (type == TransactionTypeEnum.TRANSFER.getType()) {
            if (StringUtils.isNotBlank(extra)) {
                return false;
            }
            verifyTransferTransaction((TransferTx) tx);
        } else if (type == TransactionTypeEnum.TRANSFER_EXTRA.getType()) {
            if (StringUtils.isBlank(extra)) {
                return false;
            }
            verifyTransferTransaction((TransferTx) tx);
        } else {
            //TODO  yuguojia other tx types
        }
        return true;
    }

    private boolean verifyTransferTransaction(TransferTx tx) {
        List<TransferInput> inputs = tx.getInputs();
        List<TransferOutput> outputs = tx.getOutputs();
        String hash = tx.getHash();
        if (CollectionUtils.isEmpty(inputs) || CollectionUtils.isEmpty(outputs)) {
            return false;
        }
        for (TransferInput input : inputs) {
            if (!input.valid()) {
                return false;
            }
        }
        for (TransferOutput output : outputs) {
            if (!output.valid()) {
                return false;
            }
        }
        return verifyInputs(inputs, tx.getUnLockScripts(), hash);
    }

    private boolean verifyInputs(List<TransferInput> inputs, List<UnLockScript> unLockScripts, String hash) {
        int size = inputs.size();
        if (size != unLockScripts.size()) {
            return false;
        }

        TransferInput input = null;
        UnLockScript unLockScript = null;
        for (int i = 0; i < size; i++) {
            input = inputs.get(i);
            if (null == input) {
                return false;
            }
            unLockScript = unLockScripts.get(i);
            if (null == unLockScript) {
                return false;
            }
            TransactionIndex preTxIndex = transactionIndexData.get(input.getPrevOut().getHash());
            if (preTxIndex == null) {
                //if the transactionIndex is not exist,
                //so local data is not right
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
                    return false;
                }
                BlockIndex blockIndex = blockIndexData.get(block.getHeight());
                if (blockIndex == null) {
                    //if the blockIndex is not exist,
                    //so local data is not right
                    return false;
                }
                boolean best = blockIndex.isBest(txHash);
                if (best) {
                    return false;
                }
            }

            if (unLockScript == null) {
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

    public BaseTx buildCoinBasePreMine(BigDecimal amount, String address, long lockTime, short version) {
        if (amount == null) {
            throw new RuntimeException("please set the amount");
        }
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new RuntimeException("the amount need more than zero");
        }
        if (StringUtils.isBlank(address)) {
            throw new RuntimeException("the address can not be blank");
        }
        TransferTx transaction = new TransferTx();
        transaction.setVersion(version);
        transaction.setType(TransactionTypeEnum.COINBASE_PREPARED_MINE.getType());
        transaction.setLockTime(lockTime);

        TransferOutput transferOutput = new TransferOutput();
        transferOutput.setAmount(amount);
        LockScript lockScript = new LockScript();
        lockScript.setAddress(address);
        transferOutput.setLockScript(lockScript);
        List emptyList = Lists.newArrayList();
        emptyList.add(transferOutput);
        transaction.setOutputs(emptyList);
        transaction.setSign(ECKey.signMessage(transaction.getHash(), peerKeyPair.getPriKey()));
        return transaction;
    }

    public TransferTx buildCoinBaseMine(long lockTime, short version) {
        LOGGER.info("begin to build coinBase transaction");
        BlockIndex lastBlockIndex = blockService.getLastBlockIndex();
        if (lastBlockIndex == null) {
            throw new RuntimeException("can not find last blockIndex");
        }
        String blockHash = lastBlockIndex.getBestBlockHash();
        LOGGER.info("the current height is {} and blockHash is {}", lastBlockIndex.getHeight(), blockHash);
        Block block = blockData.get(blockHash);
        if (block == null) {
            throw new RuntimeException("can not find block with blockHash " + blockHash);
        }
        PubKeyAndSignaturePair minerPKSig = block.getMinerPKSig();
        if (minerPKSig == null) {
            throw new RuntimeException("can not find miner PK sig from block");
        }
        String pubKey = minerPKSig.getPubKey();
        String address = null;
        address = ECKey.pubKey2Base58Address(pubKey);


        //todo yangyi calculate reward for the miner
        BigDecimal amount = BigDecimal.ONE;

        TransferTx transaction = new TransferTx();
        transaction.setVersion(version);
        transaction.setType(TransactionTypeEnum.COINBASE_MINE.getType());
        transaction.setLockTime(lockTime);

        TransferOutput transferOutput = new TransferOutput();
        transferOutput.setAmount(amount);
        LockScript lockScript = new LockScript();
        lockScript.setAddress(address);
        transferOutput.setLockScript(lockScript);
        List emptyList = Lists.newArrayList();
        emptyList.add(transferOutput);
        transaction.setOutputs(emptyList);
        return transaction;
    }

//    public Transaction buildCoinBaseSigner(long lockTime, short version) {
//        LOGGER.info("begin to build coinBase transaction");
//        BlockIndex lastBlockIndex = blockService.getLastBlockIndex();
//        if (lastBlockIndex == null) {
//            throw new RuntimeException("can not find last blockIndex");
//        }
//        String blockHash = lastBlockIndex.getBestBlockHash();
//        LOGGER.info("the current height is {} and blockHash is {}", lastBlockIndex.getHeight(), blockHash);
//        Block block = blockData.get(blockHash);
//        if (block == null) {
//            throw new RuntimeException("can not find block with blockHash " + blockHash);
//        }
//        Transaction transaction = new Transaction();
//        transaction.setVersion(version);
//        transaction.setType(TransactionTypeEnum.COINBASE_SIGNER.getType());
//        transaction.setLockTime(lockTime);
//
//        List<PubKeyAndSignaturePair> otherPKSigs = block.getOtherPKSigs();
//        List emptyList = Lists.newArrayList();
//        if (CollectionUtils.isNotEmpty(otherPKSigs)) {
//            otherPKSigs.forEach(pubKeyAndSignaturePair -> {
//                String pubKey = pubKeyAndSignaturePair.getPubKey();
//                String address = null;
//
//                address = ECKey.pubKey2Base58Address(pubKey);
//                //todo yangyi calculate reward for the signer
//                BigDecimal amount = BigDecimal.ONE;
//
//                TransferOutput transactionOutput = new TransferOutput();
//                transactionOutput.setAmount(amount);
//                LockScript lockScript = new LockScript();
//                lockScript.setAddress(address);
//                transactionOutput.setLockScript(lockScript);
//                emptyList.add(transactionOutput);
//
//            });
//        }
//        transaction.setOutputs(emptyList);
//        return transaction;
//    }

    //for test when there haven't wallet
//    public TransferTx buildTransfer(BigDecimal amount, String address, long lockTime, short version, String extra) {
//        if (amount == null) {
//            throw new RuntimeException("please set the amount");
//        }
//        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
//            throw new RuntimeException("the amount need more than zero");
//        }
//        ECKey casKey = ECKey.fromPrivateKey(peerKeyPair);
//        TransferTx tx = new TransferTx();
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
//        List<TransferOutput> newOutpt = new ArrayList<>();
//        List<TransferInput> newInput = new ArrayList<>();
//        tx.setInputs(newInput);
//        tx.setOutputs(newOutpt);
//
//        TransferOutput transactionOutput = new TransferOutput();
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
//            TransferInput transactionInput = new TransferInput();
//            TxOutPoint transactionOutPoint = new TxOutPoint();
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
//                TransferOutput temp = new TransferOutput();
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


    public void receivedTransaction(BaseTx data) {
        String hash = data.getHash();
        LOGGER.info("receive a new transaction from remote with hash {} and data {}", hash, data);
        Map<String, BaseTx> transactionMap = txCacheManager.getTransactionMap();
        if (transactionMap.containsKey(hash)) {
            LOGGER.info("the transaction is exist in cache with hash {}", hash);
            return;
        }
        TransactionIndex transactionIndex = transactionIndexData.get(hash);
        if (transactionIndex != null) {
            LOGGER.info("the transaction is exist in block with hash {}", hash);
            return;
        }
        boolean valid = this.valid(data);
        if (!valid) {
            LOGGER.info("the transaction is not valid {}", data);
            return;
        }
        txCacheManager.addTransaction(data);
        broadcastTransaction(data);
    }

    public void broadcastTransaction(BaseTx data) {
        BroadcastMessageEntity entity = new BroadcastMessageEntity();
        entity.setData(JSON.toJSONString(data));
        entity.setVersion(EntityType.TRANSACTION_BROADCAST.getVersion());
        entity.setType(EntityType.TRANSACTION_BROADCAST.getType());
        Application.EVENT_BUS.post(new BroadcastEvent(entity));
        LOGGER.info("broadcast transaction success");
    }

}
