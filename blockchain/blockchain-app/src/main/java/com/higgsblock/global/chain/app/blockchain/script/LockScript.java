package com.higgsblock.global.chain.app.blockchain.script;

import com.higgsblock.global.chain.common.entity.BaseSerializer;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;

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

}