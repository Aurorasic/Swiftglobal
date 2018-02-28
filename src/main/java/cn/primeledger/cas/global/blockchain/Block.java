package cn.primeledger.cas.global.blockchain;

import cn.primeledger.cas.global.blockchain.transaction.Transaction;
import cn.primeledger.cas.global.entity.BaseSerializer;
import com.google.common.base.Charsets;
import com.google.common.hash.HashFunction;
import com.google.common.hash.Hashing;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.util.Strings;

import java.util.List;

/**
 * @author yuguojia
 * @create 2018-02-22
 **/
public class Block extends BaseSerializer {
    /**
     * the number of block struct
     */
    @Getter
    private short version;

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
     * block height begin with 1
     */
    @Getter
    private long height;

    /**
     * the hash of this block
     */
    private transient String hash;

    /**
     * public keys and signatures of pairs
     */
    @Setter
    @Getter
    private List<PubKeyAndSignaturePair> pubKeyAndSignaturePairs;

    private Block(short version, long blockTime, String prevBlockHash, List<Transaction> transactions, long height) {
        this.version = version;
        this.blockTime = blockTime;
        this.prevBlockHash = prevBlockHash;
        this.transactions = transactions;
        this.height = height;
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

    /**
     * get hash of block
     *
     * @return
     */
    public String getHash() {
        if (StringUtils.isBlank(hash)) {
            HashFunction function = Hashing.sha256();
            StringBuilder builder = new StringBuilder();
            builder.append(function.hashInt(version))
                    .append(function.hashLong(blockTime))
                    .append(function.hashString(null == prevBlockHash ? Strings.EMPTY : prevBlockHash, Charsets.UTF_8))
                    .append(getTransactionsHash());
            hash = function.hashString(builder.toString(), Charsets.UTF_8).toString();
        }
        return hash;
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
            return new Block(version, blockTime, prevBlockHash, transactions, height);
        }
    }
}