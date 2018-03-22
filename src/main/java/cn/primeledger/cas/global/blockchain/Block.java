package cn.primeledger.cas.global.blockchain;

import cn.primeledger.cas.global.Application;
import cn.primeledger.cas.global.blockchain.transaction.Transaction;
import cn.primeledger.cas.global.consensus.NodeSelector;
import cn.primeledger.cas.global.entity.BaseBizEntity;
import com.alibaba.fastjson.annotation.JSONField;
import com.google.common.base.Charsets;
import com.google.common.hash.HashFunction;
import com.google.common.hash.Hashing;
import lombok.*;
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
@NoArgsConstructor
@Data
@Slf4j
public class Block extends BaseBizEntity {

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

    /**
     * public keys and signatures of pairs.
     * The first pk and sig is the miner's
     */
    @Setter
    @Getter
    private List<BlockWitness> blockWitnesses = new ArrayList<>();


    @Setter
    @Getter
    private List<BlockWitness> blockMiner = new ArrayList<>();

    @Getter
    @Setter
    private List<String> nodes;

    private Block(short version, long blockTime, String prevBlockHash, List<Transaction> transactions, long height, List<String> nodes) {
        this.setVersion(version);
        this.blockTime = blockTime;
        this.prevBlockHash = prevBlockHash;
        this.transactions = transactions;
        this.height = height;
        this.nodes = nodes;
    }

    public void initMinerPkSig(String pubKey, String signature) {
        //blockWitnesses = new LinkedList<>();
        BlockWitness pair = new BlockWitness(pubKey, signature, null);
        blockMiner.add(pair);
    }

    public Transaction getTransactionByHash(String txHash) {
        for (Transaction transaction : transactions) {
            if (StringUtils.equals(transaction.getHash(), txHash)) {
                return transaction;
            }
        }
        return null;
    }

    public void addMinerSignature(String pubKey, String signature, String blockHash) {
        BlockWitness pair = new BlockWitness(pubKey, signature, blockHash);
        blockMiner.add(pair);
    }

    public void addWitnessSignature(String pubKey, String signature, String blockHash) {
        BlockWitness pair = new BlockWitness(pubKey, signature, blockHash);
        blockWitnesses.add(pair);
    }

    public BlockWitness getMinerFirstPKSig() {
        if (!CollectionUtils.isEmpty(blockMiner)) {
            return blockMiner.get(0);
        }
        return null;
    }

    public BlockWitness getMinerSecondPKSig() {
        if (!CollectionUtils.isEmpty(blockMiner) && blockMiner.size() >= 2) {
            return blockMiner.get(1);
        }
        return null;
    }

//    public List<BlockWitness> getOtherPKSigs() {
//        if (!CollectionUtils.isEmpty(blockWitnesses)) {
//            ArrayList<BlockWitness> othersPair = new ArrayList<BlockWitness>();
//            for (int i = 1; i < blockWitnesses.size(); i++) {
//                othersPair.add(blockWitnesses.get(i));
//            }
//            return othersPair;
//        }
//        return null;
//    }

    public boolean isContainOtherPK(String oneSigPubKey) {
        List<BlockWitness> otherPKSigs = getBlockWitnesses();
        for (BlockWitness pair : otherPKSigs) {
            if (StringUtils.equals(oneSigPubKey, pair.getPubKey())) {
                return true;
            }
        }
        return false;
    }

    public List<Transaction> getSysTransactions() {
        List<Transaction> sysTransactions = new LinkedList<>();
//        if (CollectionUtils.isNotEmpty(transactions)) {
//            transactions.forEach(tx -> {
//                if (tx.isSysTransaction()) {
//                    sysTransactions.add(tx);
//                }
//            });
//        }
        return sysTransactions;
    }

    public static BlockBuilder builder() {
        return new BlockBuilder();
    }

    public boolean isgenesisBlock() {
        if (height == 1 && prevBlockHash == null) {
            return true;
        }
        return false;
    }

    public boolean isPreBlock() {
        if (height <= Application.PRE_BLOCK_COUNT) {
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
                    .append(getTransactionsHash());
            nodes.forEach(address -> builder.append(address));
            hash = function.hashString(builder.toString(), Charsets.UTF_8).toString();
        }
        return hash;
    }

    public String getSignedHash() {
        if (StringUtils.isBlank(hash)) {
            HashFunction function = Hashing.sha256();
            StringBuilder builder = new StringBuilder();
            builder.append(function.hashInt(getVersion()))
                    .append(function.hashLong(blockTime))
                    .append(function.hashString(null == prevBlockHash ? Strings.EMPTY : prevBlockHash, Charsets.UTF_8))
                    .append(getTransactionsHash());
            BlockWitness firstPKSig = getMinerFirstPKSig();
            if (firstPKSig != null) {
                builder.append(firstPKSig.getBlockWitnessHash());
            }
            List<BlockWitness> blockWitnesses = getBlockWitnesses();
            if (CollectionUtils.isNotEmpty(blockWitnesses)) {
                blockWitnesses.forEach(blockWitness -> {
                    builder.append(blockWitness.getBlockWitnessHash());
                });
            }
            nodes.forEach(address -> builder.append(address));
            hash = function.hashString(builder.toString(), Charsets.UTF_8).toString();
        }
        LOGGER.info("the block signedHash is {}", hash);
        return hash;
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

    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    public static class BlockBuilder {

        private short version;
        private long blockTime;
        private String prevBlockHash;
        private List<Transaction> transactions;
        private long height;
        private List<String> nodes;

        public BlockBuilder nodes(List<String> nodes) {
            this.nodes = nodes;
            return this;
        }

        public BlockBuilder version(short version) {
            this.version = version;
            return this;
        }

        public BlockBuilder blockTime(long blockTime) {
            this.blockTime = blockTime;
            return this;
        }

        public BlockBuilder prevBlockHash(String prevBlockHash) {
            this.prevBlockHash = prevBlockHash;
            return this;
        }

        public BlockBuilder transactions(List<Transaction> transactions) {
            this.transactions = transactions;
            return this;
        }

        public BlockBuilder height(long height) {
            this.height = height;
            return this;
        }

        public Block build() {
            return new Block(version, blockTime, prevBlockHash, transactions, height, nodes);
        }
    }

    @JSONField(serialize = false)
    public boolean isDPosEndHeight() {
        long num = NodeSelector.BATCHBLOCKNUM;
        if ((height - 1) % num == 0) {
            return true;
        }
        return false;
    }

    @JSONField(serialize = false)
    public List<String> getWitnessBlockHashList() {
        LinkedList<String> result = new LinkedList<>();
        for (BlockWitness blockWitness : blockWitnesses) {
            result.add(blockWitness.getBlockHash());
        }
        return result;
    }
}