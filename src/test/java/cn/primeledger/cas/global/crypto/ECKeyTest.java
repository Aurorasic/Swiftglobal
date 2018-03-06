package cn.primeledger.cas.global.crypto;

import cn.primeledger.cas.global.utils.Sha256Hash;
import org.junit.Test;

import java.math.BigInteger;
import java.util.Date;

import static org.junit.Assert.assertTrue;

public class ECKeyTest {

    public static final String pubkey = "02d2506ecc653a2b1861f7dfd0d4c0f0c9d8bdf448f1643ed5179a513355116407";
    public static final String prieky = "2712a751afd0e9a14148e07625595a21ee42b228a06029c2197c898ee1d53227";
    public static final String data = "asdasdasd";
    public static final String signature = "H7xgxcZN44LODIu6FkW0/uHUgBUOlt18gO4UkM8eoT0tdvPeM9SiIRov0MqtdrRWKqvauwi6KTITanITXytzilo=";
    public static final String addr = "1EhGXxcJQFrNMix2L7sKbmBSV7jZriCgMg";

    /**
     * 生成需要测试的信息
     */
    @Test
    public void createData() {
        ECKey ecKey = new ECKey();
        String data = "asdasdasd";
        System.out.println("ecKey.getPublicKeyAsHex() = " + ecKey.getKeyPair().getPubKey());
        System.out.println("ecKey.getPrivateKeyAsHex() = " + ecKey.getKeyPair().getPriKey());
        String sign = ecKey.signMessage(data);
        System.out.println("ecKey.signMessage = " + sign);
        System.out.println("ecKey.toBase58Address() = " + ecKey.toBase58Address());
        if (ecKey.verifySign(data, sign)) {
            System.out.println(true);
        }else {
            System.out.println(false);
        }
    }


    /**
     * 公钥转地址
     *
     * @throws Exception
     */
    @Test
    public void pubkey2Base58Address() throws Exception {
        assertTrue(addr.equals(ECKey.pubKey2Base58Address(pubkey)));
    }

    /**
     * 验证公钥和地址是否一致
     *
     * @throws Exception
     */
    @Test
    public void verifyPubjeyAndAddr() throws Exception {
        assertTrue(ECKey.checkPubKeyAndAddr(addr, pubkey));
    }

    /**
     * 验证签名，pubkey,message
     *
     * @throws Exception
     */
    @Test
    public void verify() throws Exception {
        assertTrue(ECKey.verifySign(data, signature, pubkey));
    }

    /**
     * 签名
     *
     * @throws Exception
     */
    @Test
    public void signMessage() throws Exception {
        ECKey ecKey = ECKey.fromPrivateKey(prieky);
        assertTrue(signature.equals(ecKey.signMessage(data)));
    }

    /**
     * @throws Exception
     */
    @Test
    public void verifyMessage() throws Exception {
        ECKey ecKey = ECKey.fromPublicKeyOnly(pubkey);
        ecKey.verifySign(data, signature);
    }

    /**
     * @throws Exception
     */
    @Test
    public void toBase58Address() throws Exception {
        ECKey ecKey = ECKey.fromPublicKeyOnly(pubkey);
        assertTrue(addr.equals(ecKey.toBase58Address()));
    }

    /**
     * 计算id的生成规则
     */
    @Test
    public void computeTxid() {
        String fromAddr = "1EhGXxcJQFrNMix2L7sKbmBSV7jZriCgMg";
        String toAddr = "1EhGXxcJQFrNMix2L7sKbmBSV7jZriCgHs";
        String amount = "1233.012389";
        for (int i = 0; i < 10; i++) {
            String data = fromAddr + new Date().getTime() + toAddr + amount + i;
            System.out.println(data);
            Sha256Hash hash = Sha256Hash.wrapReversed(Sha256Hash.hashTwice(data.getBytes()));
            System.out.println(hash.toString());
        }
    }

    @Test
    public void test12() {
        BigInteger a = new BigInteger("12asdfafafooiihi");
        System.out.println(a.toString());
    }

}