package com.higgsblock.global.chain.app.blockchain.script;

import com.alibaba.fastjson.annotation.JSONType;
import com.google.common.base.Charsets;
import com.google.common.hash.HashFunction;
import com.google.common.hash.Hashing;
import com.higgsblock.global.chain.common.entity.BaseSerializer;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;

/**
 * P2PKH or P2SH or multi-sig address script for out-save coin locking
 *
 * @author yuguojia
 * @create 2018-02-26
 **/
@Getter
@Setter
@NoArgsConstructor
@Slf4j
@JSONType(includes = {"type", "address"})
public class LockScript extends BaseSerializer {
    /**
     * lock script type such as P2PKH or P2SH
     */
    private short type;
    private String address;

    public String getHash() {
        HashFunction function = Hashing.sha256();
        StringBuilder builder = new StringBuilder()
                .append(function.hashInt(type))
                .append(function.hashString(null == address ? StringUtils.EMPTY : address, Charsets.UTF_8));
        return function.hashString(builder.toString(), Charsets.UTF_8).toString();
    }

    public boolean valid() {
        if (StringUtils.isEmpty(address)) {
            return false;
        }
        return true;
    }

}