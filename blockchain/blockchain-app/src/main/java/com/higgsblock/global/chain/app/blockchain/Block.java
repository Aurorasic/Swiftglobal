package com.higgsblock.global.chain.app.blockchain;

import com.alibaba.fastjson.annotation.JSONField;
import com.google.common.base.Charsets;
import com.google.common.base.Objects;
import com.google.common.hash.HashFunction;
import com.google.common.hash.Hashing;
import com.higgsblock.global.chain.app.blockchain.transaction.Transaction;
import com.higgsblock.global.chain.app.blockchain.transaction.UTXO;
import com.higgsblock.global.chain.app.common.message.Message;
import com.higgsblock.global.chain.app.consensus.NodeManager;
import com.higgsblock.global.chain.app.constants.EntityType;
import com.higgsblock.global.chain.app.entity.BaseBizEntity;
import com.higgsblock.global.chain.app.utils.JsonSizeCounter;
import com.higgsblock.global.chain.app.utils.SizeCounter;
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
@Message(EntityType.BLOCK)
@NoArgsConstructor
@Data
@Slf4j
public class Block extends BaseBizEntity {
    private static final int LIMITED_SIZE = 1024 * 1024 * 1;

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


    @Override
    public boolean valid() {
        if (version < 0) {
            return false;
        }
        if (height < 0) {
            return false;
        }
        if (blockTime < 0) {
            return false;
        }
        if (StringUtils.isEmpty(pubKey)) {
            return false;
        }
        if (transactions == null || transactions.size() <= 1) {
            return false;
        }
        for (Transaction transaction : transactions) {
            if (!transaction.valid()) {
                LOGGER.error("transaction is error ", transaction);
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

    public void addWitnessSignature(String pubKey, String signature, String blockHash) {
        BlockWitness pair = new BlockWitness(pubKey, signature, blockHash);
        otherWitnessSigPKS.add(pair);
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

    public String getSignedHash() {
        HashFunction function = Hashing.sha256();
        StringBuilder builder = new StringBuilder();
        builder.append(function.hashInt(getVersion()))
                .append(function.hashLong(blockTime))
                .append(function.hashString(null == prevBlockHash ? Strings.EMPTY : prevBlockHash, Charsets.UTF_8))
                .append(getTransactionsHash())
                .append(function.hashString(null == pubKey ? Strings.EMPTY : pubKey, Charsets.UTF_8));
        BlockWitness firstPKSig = getMinerFirstPKSig();
        if (firstPKSig != null) {
            builder.append(firstPKSig.getBlockWitnessHash());
        }
        List<BlockWitness> blockWitnesses = getOtherWitnessSigPKS();
        if (CollectionUtils.isNotEmpty(blockWitnesses)) {
            blockWitnesses.forEach(blockWitness -> {
                builder.append(blockWitness.getBlockWitnessHash());
            });
        }
        String signHash = function.hashString(builder.toString(), Charsets.UTF_8).toString();
        LOGGER.info("the block signedHash is {}", signHash);
        return signHash;
    }

    public boolean containsSpendUTXO(String utxoKey) {
        for (Transaction tx : transactions) {
            if (tx.containsSpendUTXO(utxoKey)) {
                return true;
            }
        }
        return false;
    }

    public UTXO getAddedUTXOByKey(String utxoKey) {
        for (Transaction tx : transactions) {
            UTXO utxo = tx.getAddedUTXOByKey(utxoKey);
            if (utxo != null) {
                return utxo;
            }
        }
        return null;
    }

    public boolean isPowerHeight(long h) {
        if (height >= h) {
            if (height % h == 0) {
                return true;
            }
        }

        return false;
    }

    public boolean isEmptyTransactions() {
        if (CollectionUtils.isEmpty(transactions)) {
            return true;
        }
        return false;
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

    @JSONField(serialize = false)
    public boolean isDPosEndHeight() {
        long num = NodeManager.BATCH_BLOCK_NUM;
        if ((height - 1) % num == 0) {
            return true;
        }
        return false;
    }

    @JSONField(serialize = false)
    public List<String> getWitnessBlockHashList() {
        LinkedList<String> result = new LinkedList<>();
        for (BlockWitness blockWitness : otherWitnessSigPKS) {
            result.add(blockWitness.getBlockHash());
        }
        return result;
    }

    public boolean sizeAllowed() {
        SizeCounter sizeCounter = new JsonSizeCounter();
        return sizeCounter.calculateSize(this) <= LIMITED_SIZE;
    }

    public int getWitnessSigCount() {
        return otherWitnessSigPKS.size();
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

}