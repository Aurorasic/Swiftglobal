package com.higgsblock.global.chain.app.blockchain;

import com.alibaba.fastjson.annotation.JSONField;
import com.google.common.base.Charsets;
import com.google.common.base.Objects;
import com.google.common.hash.HashFunction;
import com.google.common.hash.Hashing;
import com.higgsblock.global.chain.app.blockchain.transaction.Transaction;
import com.higgsblock.global.chain.app.blockchain.transaction.UTXO;
import com.higgsblock.global.chain.app.common.constants.MessageType;
import com.higgsblock.global.chain.app.common.message.Message;
import com.higgsblock.global.chain.app.utils.ISizeCounter;
import com.higgsblock.global.chain.app.utils.JsonSizeCounter;
import com.higgsblock.global.chain.common.entity.BaseSerializer;
import com.higgsblock.global.chain.crypto.ECKey;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
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
public class Block extends BaseSerializer {
    private static final int LIMITED_SIZE = 1024 * 1024 * 1;

    private int version;

    /**
     * block height begin with 1
     */
    @Getter
    private long height;

    /**
     * the hash of this block
     */
    private String hash;

    /**
     * the timestamp of this block created
     */
    @Getter
    private long blockTime;

    /**
     * the hash of prev block
     */
    @Getter
    private String prevBlockHash;

    /**
     * transactions in this block
     */
    @Getter
    private List<Transaction> transactions;

    private String pubKey;


    /**
     * signature and pubkey list whose mined this block
     */
    @Setter
    @Getter
    private List<BlockWitness> minerSelfSigPKs = new ArrayList<>();

    /**
     * witness signature and pubkey list who sig this block for calculating score
     */
    @Setter
    @Getter
    private List<BlockWitness> otherWitnessSigPKS = new ArrayList<>();

    @Setter
    @Getter
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
        if (StringUtils.isEmpty(pubKey)) {
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
        if (minerSelfSigPKs.size() < 1) {
            return false;
        }
        BlockWitness bws = minerSelfSigPKs.get(0);
        if (!pubKey.equals(bws.getPubKey())) {
            return false;
        }
        if (!ECKey.verifySign(getHash(), bws.getSignature(), bws.getPubKey())) {
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

    public void initMinerPkSig(String pubKey, String signature) {
        BlockWitness pair = new BlockWitness(pubKey, signature, null);
        minerSelfSigPKs.add(pair);
    }

    public Transaction getTransactionByHash(String txHash) {
        for (Transaction transaction : transactions) {
            if (StringUtils.equals(transaction.getHash(), txHash)) {
                return transaction;
            }
        }
        return null;
    }

    public BlockWitness getMinerFirstPKSig() {
        if (!CollectionUtils.isEmpty(minerSelfSigPKs)) {
            return minerSelfSigPKs.get(0);
        }
        return null;
    }

    public boolean isGenesisBlock() {
        if (height == 1 && prevBlockHash == null) {
            return true;
        }
        return false;
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
                    .append(function.hashString(null == pubKey ? Strings.EMPTY : pubKey, Charsets.UTF_8));
            hash = function.hashString(builder.toString(), Charsets.UTF_8).toString();
        }
        return hash;
    }

    @JSONField(serialize = false)
    public List<String> getSpendUTXOKeys() {
        List result = new LinkedList();
        for (Transaction tx : transactions) {
            result.addAll(tx.getSpendUTXOKeys());
        }

        return result;
    }

    @JSONField(serialize = false)
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

    @JSONField(serialize = false)
    public String getSimpleInfo() {
        return String.format("height=%s,hash=%s", height, getHash());
    }

    @JSONField(serialize = false)
    public String getSimpleInfoSuffix() {
        return String.format(" for height=%s,hash=%s", height, getHash());
    }
}