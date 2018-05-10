package com.higgsblock.global.chain.app.script;

import com.higgsblock.global.chain.app.blockchain.transaction.ScriptTypeEnum;
import com.higgsblock.global.chain.common.entity.BaseSerializer;
import com.higgsblock.global.chain.crypto.ECKey;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;

import java.util.List;

/**
 * P2PKH or P2SH or multi-sig address script for out-put coin locking
 *
 * @author yuguojia
 * @create 2018-02-26
 **/
@Getter
@Setter
@NoArgsConstructor
@Slf4j
public class LockScript extends BaseSerializer {
    /**
     * lock script type such as P2PKH or P2SH
     */
    private short type;
    private String address;

    public boolean valid() {
        if (StringUtils.isEmpty(address)) {
            return false;
        }
        return true;
    }

    //TODO:  zhao xiaogang add valid
    public boolean validLock(List<String> pubKeys) {
        if (type == ScriptTypeEnum.P2PKH.getType()) {
            if (CollectionUtils.isEmpty(pubKeys)) {
                LOGGER.warn("pubKeys is empty");
                return false;
            }

            String tempAddress = ECKey.pubKey2Base58Address(pubKeys.get(0));
            if (StringUtils.equals(address, tempAddress)) {
                return true;
            }
        }

        return false;
    }
}