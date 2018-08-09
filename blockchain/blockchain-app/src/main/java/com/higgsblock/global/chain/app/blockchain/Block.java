package com.higgsblock.global.chain.app.blockchain;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.alibaba.fastjson.annotation.JSONField;
import com.alibaba.fastjson.annotation.JSONType;
import com.google.common.base.Charsets;
import com.google.common.base.Objects;
import com.google.common.collect.Lists;
import com.google.common.hash.HashFunction;
import com.google.common.hash.Hashing;
import com.higgsblock.global.chain.app.blockchain.script.LockScript;
import com.higgsblock.global.chain.app.blockchain.script.UnLockScript;
import com.higgsblock.global.chain.app.blockchain.transaction.*;
import com.higgsblock.global.chain.app.common.constants.MessageType;
import com.higgsblock.global.chain.app.common.message.Message;
import com.higgsblock.global.chain.app.utils.ISizeCounter;
import com.higgsblock.global.chain.app.utils.JsonSizeCounter;
import com.higgsblock.global.chain.common.entity.BaseSerializer;
import com.higgsblock.global.chain.common.utils.Money;
import com.higgsblock.global.chain.crypto.ECKey;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.util.Strings;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * @author yuguojia
 * @create 2018-02-22
 **/
@Message(MessageType.BLOCK)
@NoArgsConstructor
@Data
@Slf4j
@JSONType(includes = {"version", "height", "blockTime", "prevBlockHash", "transactions", "minerSigPair", "witnessSigPairs", "voteVersion"})
public class Block extends BaseSerializer {
    private static final int LIMITED_SIZE = 1024 * 1024 * 1;

    private int version;

    /**
     * block height begin with 1
     */
    private long height;

    /**
     * the hash of this block
     */
    private String hash;

    /**
     * the timestamp of this block created
     */
    private long blockTime;

    /**
     * the hash of prev block
     */
    private String prevBlockHash;

    /**
     * transactions in this block
     */
    private List<Transaction> transactions;

    /**
     * signature and pubkey whose mined this block
     */
    private SignaturePair minerSigPair;

    /**
     * witness signature and pubkey list who sig this block for calculating score
     */
    private List<SignaturePair> witnessSigPairs = new ArrayList<>();

    private int voteVersion;

    public boolean valid() {
        if (version < 0) {
            return false;
        }
        if (height <= 0) {
            return false;
        } else if (height == 1L) {
            if (null != prevBlockHash) {
                return false;
            }
        } else {
            if (null == prevBlockHash) {
                return false;
            }
        }
        if (blockTime < 0) {
            return false;
        }
        if (height == 1L) {
            if (transactions.size() < 1) {
                return false;
            }
        } else {
            if (transactions == null || transactions.size() <= 1) {
                return false;
            }
        }

        for (Transaction transaction : transactions) {
            if (!transaction.valid()) {
                LOGGER.error("transaction is error, hash={}", transaction.getHash());
                return false;
            }
        }
        if (minerSigPair == null || !minerSigPair.valid()) {
            return false;
        }
        if (!ECKey.verifySign(getHash(), minerSigPair.getSignature(), minerSigPair.getPubKey())) {
            return false;
        }
        if (!sizeAllowed()) {
            return false;
        }
        return true;
    }

    private Block(short version, long blockTime, String prevBlockHash, List<Transaction> transactions, long height) {
        setVersion(version);
        this.blockTime = blockTime;
        this.prevBlockHash = prevBlockHash;
        this.transactions = transactions;
        this.height = height;
    }

    public void setMinerSigPK(String pubKey, String signature) {
        SignaturePair pair = new SignaturePair(pubKey, signature);
        minerSigPair = pair;
    }

    public Transaction getTransactionByHash(String txHash) {
        for (Transaction transaction : transactions) {
            if (StringUtils.equals(transaction.getHash(), txHash)) {
                return transaction;
            }
        }
        return null;
    }

    public boolean isGenesisBlock() {
        if (height == 1 && prevBlockHash == null) {
            return true;
        }
        return false;
    }

    public String getPubKey() {
        return minerSigPair == null ? null : minerSigPair.getPubKey();
    }

    /**
     * get hash of block
     *
     * @return
     */
    public String getHash() {
        if (StringUtils.isBlank(hash)) {
            HashFunction function = Hashing.sha256();
            StringBuilder builder = new StringBuilder();
            builder.append(function.hashInt(getVersion()))
                    .append(function.hashLong(blockTime))
                    .append(function.hashString(null == prevBlockHash ? Strings.EMPTY : prevBlockHash, Charsets.UTF_8))
                    .append(getTransactionsHash())
                    .append(function.hashString(null == getPubKey() ? Strings.EMPTY : getPubKey(), Charsets.UTF_8));
            hash = function.hashString(builder.toString(), Charsets.UTF_8).toString();
        }
        return hash;
    }

    public List<String> getSpendUTXOKeys() {
        List result = new LinkedList();
        for (Transaction tx : transactions) {
            result.addAll(tx.getSpendUTXOKeys());
        }

        return result;
    }

    public List<UTXO> getAddedUTXOs() {
        List result = new LinkedList();
        for (Transaction tx : transactions) {
            result.addAll(tx.getAddedUTXOs());
        }

        return result;
    }

    /**
     * get hash of transaction list in this block
     *
     * @return
     */
    public String getTransactionsHash() {
        StringBuilder builder = new StringBuilder();
        if (CollectionUtils.isEmpty(transactions)) {
            builder.append(Hashing.sha256().hashString(Strings.EMPTY, Charsets.UTF_8));
        } else {
            transactions.forEach(transaction -> builder.append(transaction.getHash()));
        }

        return Hashing.sha256().hashString(builder.toString(), Charsets.UTF_8).toString();
    }

    public boolean sizeAllowed() {
        ISizeCounter sizeCounter = JsonSizeCounter.getJsonSizeCounter();
        return sizeCounter.calculateSize(this) <= LIMITED_SIZE;
    }

    @Override
    public boolean equals(Object other) {
        if (other == null || !(other instanceof Block)) {
            return false;
        }
        if (StringUtils.equals(getHash(), ((Block) other).getHash())) {
            return true;
        }
        return false;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(getHash());
    }

    public String getSimpleInfo() {
        return String.format("height=%s,hash=%s", height, getHash());
    }

    public String getSimpleInfoSuffix() {
        return String.format(" for height=%s,hash=%s", height, getHash());
    }

    /*public static void main(String[] args) throws Exception {
        test2(test1());
    }

    public static String test1() {
        Block block = new Block();
        block.setVoteVersion(1);
        block.setVersion(1);
        block.setHeight(100);
        block.setBlockTime(1533804553179L);
        block.setPrevBlockHash("kkkkkkkkkkkkkkkkkkkk");
        block.setMinerSigPK("ooooooooo", "555555555555555555");
        List<Transaction> transactions = Lists.newArrayList();
        transactions.add(buildTx());
        block.setTransactions(transactions);

        List<SignaturePair> signaturePairs = Lists.newArrayList();
        SignaturePair signaturePair = new SignaturePair("3333333", "99999999");
        signaturePairs.add(signaturePair);
        block.setWitnessSigPairs(signaturePairs);

        System.out.println(block.toJson());
        System.out.println("block hash = "+block.getHash());
        return block.toJson();
    }

    public static void test2(String jsonStr) {
        Block block = JSON.parseObject(jsonStr, new TypeReference<Block>() {
        });

        System.out.println(block.toJson());
        System.out.println("block hash = "+block.getHash());
    }

    public static Transaction buildTx() {
        Transaction transaction = new Transaction();

        transaction.setVersion(1);
        transaction.setExtra("123");
        transaction.setCreatorPubKey("11111");
        transaction.setLockTime(0);

        List<TransactionInput> transactionInputs = Lists.newArrayList();
        List<TransactionOutput> transactionOutputs = Lists.newArrayList();

        TransactionInput transactionInput = new TransactionInput();

        UnLockScript unLockScript = new UnLockScript();
        List<String> pkList = Lists.newArrayList();
        List<String> sigList = Lists.newArrayList();
        pkList.add("aaaaaaaaa");
        sigList.add("bbbbbbbb");
        unLockScript.setPkList(pkList);
        unLockScript.setSigList(sigList);
        transactionInput.setUnLockScript(unLockScript);

        TransactionOutPoint transactionOutPoint = new TransactionOutPoint();
        transactionOutPoint.setIndex((short) 1);
        transactionOutPoint.setHash("ccccccccccccc");
        transactionInput.setPrevOut(transactionOutPoint);

        transactionInputs.add(transactionInput);

        TransactionOutput transactionOutput = new TransactionOutput();
        Money money = new Money();
        money.setValue("1");
        money.setCurrency("cas");
        transactionOutput.setMoney(money);
        LockScript lockScript = new LockScript();
        lockScript.setAddress("aaaaaaaaaaaaaaaaaa");
        lockScript.setType((short) 1);
        transactionOutput.setLockScript(lockScript);

        transactionOutputs.add(transactionOutput);

        transaction.setOutputs(transactionOutputs);
        transaction.setInputs(transactionInputs);
        return transaction;
    }*/
}