package cn.primeledger.cas.global.box;

import org.junit.Test;

/**
 * @author Su Jiulong
 * @date 2018/2/26
 */
public class KeyPairManagerTest {
    KeyPairManager keyPairManager = new KeyPairManager();

    @Test
    public void createKeyPair() throws Exception {
        System.out.println("生成公私钥对 并进行存储后返回公钥：" + keyPairManager.createKeyPair("C://Test/cfg.json"));
    }

    @Test
    public void validateKeyPair() throws Exception {
        System.out.println("验证从配置文件中取出的公私钥对 验证通过后返回公钥：" + keyPairManager.validateKeyPair("C://Test/cfg.json"));
    }

}