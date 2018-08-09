package com.higgsblock.global.chain.app.blockchain.transaction;

import com.alibaba.fastjson.annotation.JSONType;
import com.google.common.base.Charsets;
import com.google.common.hash.HashFunction;
import com.google.common.hash.Hashing;
import com.higgsblock.global.chain.app.common.constants.MessageType;
import com.higgsblock.global.chain.app.common.message.Message;
import com.higgsblock.global.chain.app.utils.ISizeCounter;
import com.higgsblock.global.chain.app.utils.JsonSizeCounter;
import com.higgsblock.global.chain.common.entity.BaseSerializer;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.util.Strings;

import java.util.LinkedList;
import java.util.List;

/**
 * @author yuguojia
 * @date 2018/03/08
 **/
@Data
@Slf4j
@NoArgsConstructor
@Message(MessageType.TRANSACTION)
@JSONType(includes = {"version", "lockTime", "extra", "inputs", "outputs", "transactionTime", "creatorPubKey"})
public class Transaction extends BaseSerializer {

    private static final int LIMITED_SIZE_UNIT = 1024 * 100;
    private static final int EXTRA_LIMITED_SIZE_UNIT = 1024 * 10;
    private static final int INIT_VERSION = 0;

    private int version;

    /**
     * the hash of this transaction
     */
    protected String hash;
    /**
     * lock after pointed block height of time
     */
    protected long lockTime;
    /**
     * extra info for this transaction
     */
    protected String extra;
    /**
     * the sources of current spending
     */
    private List<TransactionInput> inputs;
    /**
     * transfer to other coin
     */
    private List<TransactionOutput> outputs;

    /**
     * the timestamp of this transaction created
     */
    private long transactionTime = System.currentTimeMillis();

    private String creatorPubKey;

    public boolean valid() {

        if (version < INIT_VERSION) {
            return false;
        }

        if (StringUtils.isEmpty(hash)) {
            return false;
        }

        if (StringUtils.isEmpty(creatorPubKey)) {
            return false;
        }

        if (lockTime < 0) {
            return false;
        }

        if (CollectionUtils.isNotEmpty(inputs)) {
            for (TransactionInput input : inputs) {
                if (!input.valid()) {
                    return false;
                }
            }
        }

        if (CollectionUtils.isNotEmpty(outputs)) {
            for (TransactionOutput out : outputs) {
                if (!out.valid()) {
                    return false;
                }
            }
        }
        return true;
    }

    public String getHash() {
        if (StringUtils.isBlank(hash)) {
            HashFunction function = Hashing.sha256();
            StringBuilder builder = new StringBuilder();
            builder.append(function.hashInt(version));
            builder.append(function.hashLong(transactionTime));
            builder.append(function.hashLong(lockTime));
            builder.append(function.hashString(null == extra ? Strings.EMPTY : extra, Charsets.UTF_8));
            builder.append(function.hashString(null == creatorPubKey ? Strings.EMPTY : creatorPubKey, Charsets.UTF_8));
            if (CollectionUtils.isNotEmpty(inputs)) {
                inputs.forEach((input) -> {
                    TransactionOutPoint prevOut = input.getPrevOut();
                    if (prevOut != null) {
                        builder.append(function.hashLong(prevOut.getIndex()));
                        String prevOutHash = prevOut.getHash();
                        builder.append(function.hashString(null == prevOutHash ? Strings.EMPTY : prevOutHash, Charsets.UTF_8));
                    }
                });
            } else {
                builder.append(function.hashInt(0));
            }
            if (CollectionUtils.isNotEmpty(outputs)) {
                outputs.forEach((output) -> builder.append(output.getHash()));
            } else {
                builder.append(function.hashInt(0));
            }
            hash = function.hashString(builder, Charsets.UTF_8).toString();
        }
        return hash;
    }

    public TransactionOutput getTransactionOutputByIndex(short index) {
        int size = outputs.size();
        if (size <= index + 1 || index < 0) {
            return null;
        }
        return outputs.get(index);
    }

    public boolean sizeAllowed() {
        ISizeCounter sizeCounter = JsonSizeCounter.getJsonSizeCounter();
        if (sizeCounter.calculateSize(extra) > EXTRA_LIMITED_SIZE_UNIT) {
            return false;
        }
        if (sizeCounter.calculateSize(this) > LIMITED_SIZE_UNIT) {
            return false;
        }
        return true;
    }

    public boolean isEmptyInputs() {
        if (CollectionUtils.isEmpty(inputs)) {
            return true;
        }
        return false;
    }

    public List<String> getSpendUTXOKeys() {
        List result = new LinkedList();
        if (!isEmptyInputs()) {
            for (TransactionInput input : inputs) {
                result.add(input.getPreUTXOKey());
            }
        }

        return result;
    }

    public List<UTXO> getAddedUTXOs() {
        List result = new LinkedList();
        if (CollectionUtils.isNotEmpty(outputs)) {
            final int outputSize = outputs.size();
            for (int i = 0; i < outputSize; i++) {
                TransactionOutput output = outputs.get(i);
                UTXO utxo = new UTXO(this, (short) i, output);
                result.add(utxo);
            }
        }

        return result;
    }

    public boolean containsSpendUTXO(String utxoKey) {
        if (isEmptyInputs()) {
            return false;
        }
        for (TransactionInput input : inputs) {
            if (StringUtils.equals(input.getPreUTXOKey(), utxoKey)) {
                return true;
            }
        }
        return false;
    }

    /*public static void main(String[] args) throws Exception {
        test2(test1());
    }

    public static String test1() {
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

        System.out.println(transaction.toJson());
        System.out.println("tx hash = "+transaction.getHash());
        return transaction.toJson();
    }

    public static void test2(String jsonStr) {
        Transaction transaction = JSON.parseObject(jsonStr, new TypeReference<Transaction>() {
        });
        System.out.println(transaction.toJson());
        System.out.println("tx hash = "+transaction.getHash());
    }*/
}