package com.higgsblock.global.chain.app.contract;

import com.higgsblock.global.chain.vm.core.Repository;
import com.higgsblock.global.chain.vm.util.ByteUtil;
import com.higgsblock.global.chain.vm.util.HashUtil;
import org.junit.Assert;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * @author tangkun
 * @date 2018-09-17
 */
public class RepositoryImplTest {

    public static void main(String[] args) {

        Repository blockR =   new RepositoryImpl();
        Repository  tractionR = blockR.startTracking();

        Repository contractR = tractionR.startTracking();
        byte[] key = "oxkey9".getBytes();
        contractR.saveCode(key,"oxvalue".getBytes());

        contractR.commit();
        Assert.assertEquals(blockR.getCode(key).length,0);
        tractionR.commit();
        blockR.commit();
        key = ByteUtil.xorAlignRight(key, HashUtil.sha3("code".getBytes()));
        //Assert.assertNull(((RepositoryImpl)blockR).getInDB(key));
//        Assert.assertEquals(new String(blockR.getCode(key)),"oxvalue");
        blockR.flush();
        //Assert.assertEquals(new String(((RepositoryImpl) blockR).getInDB(key)),"oxvalue");

    }



}