package cn.primeledger.cas.global.api.vo;

import cn.primeledger.cas.global.entity.BaseBizEntity;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;

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
}
