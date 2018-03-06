package cn.primeledger.cas.global.blockchain;

import cn.primeledger.cas.global.blockchain.transaction.BaseTx;
import cn.primeledger.cas.global.entity.BaseSerializer;
import com.google.common.base.Charsets;
import com.google.common.hash.HashFunction;
import com.google.common.hash.Hashing;
import lombok.*;
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
public class Block extends BaseSerializer {

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
    private List<BaseTx> transactions;

    /**
     * public keys and signatures of pairs.
     * The first pk and sig is the miner's
     */
    @Setter
    @Getter
    private List<PubKeyAndSignaturePair> pubKeyAndSignaturePairs;

    private Block(short version, long blockTime, String prevBlockHash, List<BaseTx> transactions, long height) {
        this.version = version;
        this.blockTime = blockTime;
        this.prevBlockHash = prevBlockHash;
        this.transactions = transactions;
        this.height = height;
    }


    public void initMinerPkSig(String pubKey, String signature) {
        pubKeyAndSignaturePairs = new LinkedList<>();
        PubKeyAndSignaturePair pair = new PubKeyAndSignaturePair(pubKey, signature, null);
        pubKeyAndSignaturePairs.add(pair);
    }

    public PubKeyAndSignaturePair getMinerPKSig() {
        if (!CollectionUtils.isEmpty(pubKeyAndSignaturePairs)) {
            return pubKeyAndSignaturePairs.get(0);
        }
        return null;
    }

    public List<PubKeyAndSignaturePair> getOtherPKSigs() {
        if (!CollectionUtils.isEmpty(pubKeyAndSignaturePairs)) {
            ArrayList<PubKeyAndSignaturePair> othersPair = new ArrayList<PubKeyAndSignaturePair>();
            for (int i = 1; i < pubKeyAndSignaturePairs.size(); i++) {
                othersPair.add(pubKeyAndSignaturePairs.get(i));
            }
            return othersPair;
        }
        return null;
    }

    public boolean isContainOtherPK(String oneSigPubKey) {
        List<PubKeyAndSignaturePair> otherPKSigs = getOtherPKSigs();
        for (PubKeyAndSignaturePair pair : otherPKSigs) {
            if (StringUtils.equals(oneSigPubKey, pair.getPubKey())) {
                return true;
            }
        }
        return false;
    }

    public List<BaseTx> getSysTransactions() {
        List<BaseTx> sysTransactions = new LinkedList<>();
        if (CollectionUtils.isNotEmpty(transactions)) {
            transactions.forEach(tx -> {
                if (tx.isSysTransaction()) {
                    sysTransactions.add(tx);
                }
            });
        }
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

    public boolean isHundredFirstBlock() {
        if (height > 100) {
            long n = height / 100;
            long num = n * 100;
            if (height == num + 1) {
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
        private List<BaseTx> transactions;
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

        public BlockBuilder transactions(List<BaseTx> transactions) {
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