package cn.primeledger.cas.global.crypto.model;

import cn.primeledger.cas.global.entity.BaseSerializer;
import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.Nullable;

/**
 * @author kongyu
 * @create 2018-02-26 14:22
 */
@Setter
@Getter
public class KeyPair extends BaseSerializer {
    @Nullable
    private String priKey;
    @Nullable
    private String pubKey;

    public KeyPair() {
    }

    public KeyPair(String privateKey, String publicKey) {
        this.priKey = privateKey;
        this.pubKey = publicKey;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof KeyPair)) return false;

        KeyPair keyPair = (KeyPair) o;

        if (priKey != null ? !priKey.equals(keyPair.priKey) : keyPair.priKey != null) return false;
        return pubKey != null ? pubKey.equals(keyPair.pubKey) : keyPair.pubKey == null;
    }

    @Override
    public int hashCode() {
        int result = priKey != null ? priKey.hashCode() : 0;
        result = 31 * result + (pubKey != null ? pubKey.hashCode() : 0);
        return result;
    }
}
