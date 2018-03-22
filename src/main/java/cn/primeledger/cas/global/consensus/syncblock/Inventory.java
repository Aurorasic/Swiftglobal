package cn.primeledger.cas.global.consensus.syncblock;

import cn.primeledger.cas.global.entity.BaseBizEntity;
import lombok.Data;

import java.util.Set;

/**
 * @author yuanjiantao
 * @date Created on 3/8/2018
 */
@Data
public class Inventory extends BaseBizEntity {

    private long height;

    private Set<String> hashs;
}
