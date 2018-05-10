package com.higgsblock.global.chain.app.consensus.sign.model;

import com.higgsblock.global.chain.app.entity.BaseBizEntity;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.commons.lang.StringUtils;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class WitnessSign extends BaseBizEntity {

    private String blockHash;

    private String signature;

    private String pubKey;

    @Override
    public boolean valid() {

        if (StringUtils.isEmpty(blockHash)){
            return false;
        }
        if (StringUtils.isEmpty(signature)){
            return false;
        }
        if (StringUtils.isEmpty(pubKey)){
            return false;
        }

      return true;
    }
}
