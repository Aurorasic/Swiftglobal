package com.higgsblock.global.chain.vm.util;

import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.spongycastle.util.encoders.Hex;

/**
 * @author tangkun
 * @date 2018-10-18
 */
@Slf4j
public class HashUtilTest {

    @Test
    public void testSha3() throws Exception {
        System.out.println("account subKey:{}" + Hex.toHexString(ByteUtil.xorAlignRight(HashUtil.sha3("account".getBytes()), new byte[0])));
        System.out.println("code subKey:{}" + Hex.toHexString(ByteUtil.xorAlignRight(HashUtil.sha3("code".getBytes()), new byte[0])));
        System.out.println("storage subKey:{}" + Hex.toHexString(ByteUtil.xorAlignRight(HashUtil.sha3("storage".getBytes()), new byte[0])));
    }
}