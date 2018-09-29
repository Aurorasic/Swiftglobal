package com.higgsblock.global.chain.app.blockchain.script;

import com.alibaba.fastjson.annotation.JSONType;
import com.google.common.base.Charsets;
import com.google.common.hash.HashFunction;
import com.google.common.hash.Hashing;
import com.higgsblock.global.chain.common.entity.BaseSerializer;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;

import java.util.List;

/**
 * P2PKH or P2SH or multi-sig address script for in-save coin unlocking such as
 * sig1,sig2
 * PK1,PK2,PK3
 *
 * @author yuguojia
 * @create 2018-02-26
 **/
@Getter
@Setter
@NoArgsConstructor
@JSONType(includes = {"sigList", "pkList"})
public class UnLockScript extends BaseSerializer {
    /**
     * max num of public key
     */
    public static int MAX_NUM = 21;
    /**
     * signature list
     */
    private List<String> sigList;
    /**
     * public key list
     */
    private List<String> pkList;

    public String getHash() {
        HashFunction function = Hashing.sha256();
        StringBuilder builder = new StringBuilder()
                .append(function.hashString(getListHash(pkList), Charsets.UTF_8));
        return function.hashString(builder, Charsets.UTF_8).toString();
    }

    private String getListHash(List<String> list) {
        HashFunction function = Hashing.sha256();
        if (CollectionUtils.isEmpty(list)) {
            return function.hashInt(0).toString();
        }

        StringBuilder builder = new StringBuilder();
        list.forEach(s -> builder
                .append(function.hashString(null == s ? StringUtils.EMPTY : s, Charsets.UTF_8))
        );
        return function.hashString(builder, Charsets.UTF_8).toString();
    }

    public boolean valid() {
        if (CollectionUtils.isEmpty(pkList) || CollectionUtils.isEmpty(sigList)) {
            return false;
        }
        if (sigList.size() > pkList.size()) {
            return false;
        }
        if (pkList.size() > MAX_NUM) {
            return false;
        }
        return true;
    }

}