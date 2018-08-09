package com.higgsblock.global.chain.app.blockchain.script;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.alibaba.fastjson.annotation.JSONType;
import com.higgsblock.global.chain.common.entity.BaseSerializer;
import com.higgsblock.global.chain.common.utils.Money;
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

    public boolean valid() {
        if (StringUtils.isEmpty(address)) {
            return false;
        }
        return true;
    }

    /*public static void main(String[] args) throws Exception {
        test1();
        test2();
    }

    public static void test1() {
        LockScript money = new LockScript();
        money.setAddress("aaaaaa");
        money.setType((short)1);
        System.out.println(money.toJson());
    }

    public static void test2() {
        String jsonStr = "{\"address\":\"aaaaaa\",\"type\":1}";
        LockScript lockScript = JSON.parseObject(jsonStr,new TypeReference<LockScript>(){});
        System.out.println(lockScript.toJson());
    }*/
}