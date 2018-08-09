package com.higgsblock.global.chain.app.blockchain.script;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;
import com.alibaba.fastjson.annotation.JSONType;
import com.google.common.collect.Lists;
import com.higgsblock.global.chain.common.entity.BaseSerializer;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.apache.commons.collections.CollectionUtils;

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

    public boolean valid() {
        if (CollectionUtils.isEmpty(pkList) || CollectionUtils.isEmpty(sigList)) {
            return false;
        }
        if (sigList.size() < 1) {
            return false;
        }
        if (sigList.size() > pkList.size()) {
            return false;
        }
        if (sigList.size() > MAX_NUM) {
            return false;
        }
        return true;
    }

    /*public static void main(String[] args) throws Exception {
        test1();
        test2();
    }

    public static void test1() {
        UnLockScript unLockScript = new UnLockScript();
        List<String> pkList = Lists.newArrayList();
        List<String> sigList = Lists.newArrayList();
        pkList.add("aaaaaaaaa");
        sigList.add("bbbbbbbb");
        unLockScript.setPkList(pkList);
        unLockScript.setSigList(sigList);
        System.out.println(unLockScript.toJson());
    }

    public static void test2() {
        String jsonStr = "{\"pkList\":[\"aaaaaaaaa\"],\"sigList\":[\"bbbbbbbb\"]}";
        UnLockScript unLockScript = JSON.parseObject(jsonStr,new TypeReference<UnLockScript>(){});
        System.out.println(unLockScript.toJson());
    }*/
}