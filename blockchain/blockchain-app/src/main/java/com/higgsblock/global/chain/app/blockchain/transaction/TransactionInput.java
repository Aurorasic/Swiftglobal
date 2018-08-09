package com.higgsblock.global.chain.app.blockchain.transaction;

import com.alibaba.fastjson.annotation.JSONType;
import com.higgsblock.global.chain.app.blockchain.script.UnLockScript;
import com.higgsblock.global.chain.common.entity.BaseSerializer;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author baizhengwen
 * @create 2018-03-06
 **/
@Data
@NoArgsConstructor
@JSONType(includes = {"prevOut", "unLockScript"})
public class TransactionInput extends BaseSerializer {
    /**
     * the sources of current spending
     */
    private TransactionOutPoint prevOut;

    /**
     * unlock script: signature and pk
     */
    private UnLockScript unLockScript;

    public boolean valid() {
        if (prevOut == null || unLockScript == null) {
            return false;
        }
        return prevOut.valid() && unLockScript.valid();
    }

    public String getPreUTXOKey() {
        return prevOut.getKey();
    }

    /*public static void main(String[] args) throws Exception {
        test1();
        test2();
    }

    public static void test1() {
        TransactionInput transactionInput = new TransactionInput();

        UnLockScript unLockScript = new UnLockScript();
        List<String> pkList = Lists.newArrayList();
        List<String> sigList = Lists.newArrayList();
        pkList.add("aaaaaaaaa");
        sigList.add("bbbbbbbb");
        unLockScript.setPkList(pkList);
        unLockScript.setSigList(sigList);
        transactionInput.setUnLockScript(unLockScript);

        TransactionOutPoint transactionOutPoint = new TransactionOutPoint();
        transactionOutPoint.setIndex((short) 1);
        transactionOutPoint.setHash("ccccccccccccc");
        transactionInput.setPrevOut(transactionOutPoint);

        System.out.println(transactionInput.toJson());
    }

    public static void test2() {
        String jsonStr = "{\"prevOut\":{\"hash\":\"ccccccccccccc\",\"index\":1},\"unLockScript\":{\"pkList\":[\"aaaaaaaaa\"],\"sigList\":[\"bbbbbbbb\"]}}";

        TransactionInput transactionInput = JSON.parseObject(jsonStr,new TypeReference<TransactionInput>(){});
        System.out.println(transactionInput.toJson());
    }*/
}