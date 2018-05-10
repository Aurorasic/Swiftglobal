package com.higgsblock.global.chain.app.blockchain;

import com.higgsblock.global.chain.common.entity.BaseSerializer;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.commons.lang.StringUtils;

/**
 * Paired public key and signature
 *
 * @author baizhengwen
 * @date 2018/2/22
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PubKeyAndSignPair extends BaseSerializer {
    private String pubKey;
    private String signature;

    public boolean valid() {
        if (StringUtils.isEmpty(pubKey)) {
            return false;
        }

        if (StringUtils.isEmpty(signature)) {
            return false;
        }
        return true;
    }
}
