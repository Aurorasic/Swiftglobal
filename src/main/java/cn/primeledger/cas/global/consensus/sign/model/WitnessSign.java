package cn.primeledger.cas.global.consensus.sign.model;

import cn.primeledger.cas.global.entity.BaseBizEntity;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class WitnessSign extends BaseBizEntity {

    private String blockHash;

    private String signature;

    private String pubKey;

}
