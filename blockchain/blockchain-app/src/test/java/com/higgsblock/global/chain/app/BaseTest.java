package com.higgsblock.global.chain.app;

import com.higgsblock.global.chain.crypto.ECKey;
import com.higgsblock.global.chain.crypto.KeyPair;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 * @author yangyi
 * @deta 2018/2/26
 * @description
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {Application.class})
public class BaseTest {
    public static void main(String[] args) {
        for (int i = 0; i < 5; i++) {
            ECKey ecKey = new ECKey();
            printInfo(ecKey.getKeyPair());
        }

//        ECKey ecKey = ECKey.fromPrivateKey("b8ccfcf9397fd1a150dc89bf718d446b8cd5c23b1ae363eb4a55f21050932fe6");
//        printInfo(ecKey.getKeyPair());
//
//        ecKey = ECKey.fromPrivateKey("3e61ac0b203b9a34e5667e01bf6503e5253204563d751f1c6945205b8cd5f58c");
//        printInfo(ecKey.getKeyPair());
//
//        ecKey = ECKey.fromPrivateKey("9967ff16de8145eef17e42f3f3fa009ea0c4b0a40ab93ac82291a320134e5c5a");
//        printInfo(ecKey.getKeyPair());
//
//        ecKey = ECKey.fromPrivateKey("c26fa86b76a663438b766e93b8177685d394131b484a7f6e7bdc501d127cfe53");
//        printInfo(ecKey.getKeyPair());

//        ECKey ecKey = ECKey.fromPrivateKey("0203e85eab2f5ccd3e6a458bea0ed772809dfa331778f9d57aa6aa35f1fb16922d");
//        printInfo(ecKey.getKeyPair());

    }

    public static void printInfo(KeyPair keyPair) {
        String priKey = keyPair.getPriKey();
        String pubKey = keyPair.getPubKey();
        String address = ECKey.pubKey2Base58Address(pubKey);
        System.out.println(priKey);
        System.out.println(pubKey);
        System.out.println(address);
    }
}
