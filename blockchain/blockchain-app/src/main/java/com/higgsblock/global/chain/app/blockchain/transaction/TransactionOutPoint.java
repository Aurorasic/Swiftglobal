package com.higgsblock.global.chain.app.blockchain.transaction;

import com.alibaba.fastjson.annotation.JSONType;
import com.higgsblock.global.chain.common.entity.BaseSerializer;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.apache.commons.lang.StringUtils;

/**
 * @author yuguojia
 * @create 2018-02-24
 **/
@Setter
@Getter
@NoArgsConstructor
@JSONType(includes = {"hash", "index"})
public class TransactionOutPoint extends BaseSerializer {
    /**
     * the hash of source transaction for spending
     */
    private String hash;

    /**
     * the index out of source transaction
     */
    private short index;

    public boolean valid() {
        if (StringUtils.isEmpty(hash)) {
            return false;
        }
        if (index < 0) {
            return false;
        }
        return true;
    }

    public String getKey() {
        return hash + "_" + index;
    }

    /*public static void main(String[] args) throws Exception {
        test1();
        test2();
    }

    public static void test1() {
        TransactionOutPoint transactionOutPoint = new TransactionOutPoint();
        transactionOutPoint.setHash("aaaaaaaaaaaaaaaa");
        transactionOutPoint.setIndex((short) 1);
        System.out.println(transactionOutPoint.toJson());
    }

    public static void test2() {
        String jsonStr = "{\"hash\":\"aaaaaaaaaaaaaaaa\",\"index\":1}";
        TransactionOutPoint transactionOutPoint = JSON.parseObject(jsonStr,new TypeReference<TransactionOutPoint>(){});
        System.out.println(transactionOutPoint.toJson());
    }*/
}