package cn.primeledger.cas.global.consensus.syncblock;

import cn.primeledger.cas.global.entity.BaseBizEntity;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author yuanjiantao
 * @date Created on 3/8/2018
 */
@NoArgsConstructor
@AllArgsConstructor
@Data
public class MaxHeight extends BaseBizEntity {
    private long maxHeight;
}
