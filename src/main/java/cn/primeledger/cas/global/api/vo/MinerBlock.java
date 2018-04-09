package cn.primeledger.cas.global.api.vo;

import cn.primeledger.cas.global.entity.BaseBizEntity;
import lombok.AccessLevel;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * @author kongyu
 * @date 2018-3-27 10:02
 */
@NoArgsConstructor
@Data
public class MinerBlock extends BaseBizEntity {
    /**
     * block height begin with 1
     */
    @Getter
    private long height;

    /**
     * the hash of this block
     */
    @Getter
    private String hash;

    /**
     * the timestamp of this block created
     */
    @Getter
    private long blockTime;

    @Getter
    private long blockSize;

    @Getter
    private BigDecimal earnings;

    /**
     * There is not only cas coin, a different currency is a different token.
     * If it is null, the coin is CAS.
     */
    @Getter
    private String currency;

    private MinerBlock(long height, String hash, long blockTime, long blockSize, BigDecimal earnings, String currency) {
        this.height = height;
        this.hash = hash;
        this.blockTime = blockTime;
        this.blockSize = blockSize;
        this.earnings = earnings;
        this.currency = currency;
    }

    public static MinerBlockBuilder builder() {
        return new MinerBlockBuilder();
    }

    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    public static class MinerBlockBuilder {
        private long height;
        private String hash;
        private long blockTime;
        private long blockSize;
        private BigDecimal earnings;
        private String currency;

        public MinerBlockBuilder height(long height) {
            this.height = height;
            return this;
        }

        public MinerBlockBuilder hash(String hash) {
            this.hash = hash;
            return this;
        }

        public MinerBlockBuilder blockTime(long blockTime) {
            this.blockTime = blockTime;
            return this;
        }

        public MinerBlockBuilder blockSize(long blockSize) {
            this.blockSize = blockSize;
            return this;
        }

        public MinerBlockBuilder earnings(BigDecimal earnings) {
            this.earnings = earnings;
            return this;
        }

        public MinerBlockBuilder currency(String currency) {
            this.currency = currency;
            return this;
        }

        public MinerBlock build() {
            return new MinerBlock(height, hash, blockTime, blockSize, earnings, currency);
        }
    }
}
