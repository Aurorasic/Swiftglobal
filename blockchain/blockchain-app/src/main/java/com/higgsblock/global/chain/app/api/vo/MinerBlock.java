package com.higgsblock.global.chain.app.api.vo;

import com.higgsblock.global.chain.app.entity.BaseBizEntity;
import com.higgsblock.global.chain.common.utils.Money;
import lombok.AccessLevel;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;

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

    /**
     * The money includes value and currency, a different currency is a different token.
     * If it is null, the coin is CAS.
     */
    @Getter
    private Money money;

    private MinerBlock(long height, String hash, long blockTime, long blockSize, Money money) {
        this.height = height;
        this.hash = hash;
        this.blockTime = blockTime;
        this.blockSize = blockSize;
        this.money = money;
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
        private Money money;

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

        public MinerBlockBuilder money(Money money){
            this.money = money;
            return this;
        }

        public MinerBlock build() {
            return new MinerBlock(height, hash, blockTime, blockSize,money);
        }
    }
}
