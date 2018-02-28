package cn.primeledger.cas.global.crypto.model;

import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.Nullable;

/**
 * @author kongyu
 * @create 2018-02-26 14:22
 */
@Setter
@Getter
public class KeyPair {
    @Nullable
    private String priKey;
    @Nullable
    private String pubKey;
    /**
     * pubKey hash
     */
    @Nullable
    private byte[] pubKeyHash;

    @Nullable
    private String address;

    public KeyPair() {
    }

    public KeyPair(String privateKey, String publicKey) {
        this.priKey = privateKey;
        this.pubKey = publicKey;
    }

    public KeyPair(String privateKey, String publicKey, byte[] pubKeyHash) {
        this.priKey = privateKey;
        this.pubKey = publicKey;
        this.pubKeyHash = pubKeyHash;
    }
}
