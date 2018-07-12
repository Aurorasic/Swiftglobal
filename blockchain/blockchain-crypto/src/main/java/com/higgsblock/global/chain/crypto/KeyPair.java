package com.higgsblock.global.chain.crypto;

import com.higgsblock.global.chain.common.entity.BaseSerializer;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang.StringUtils;

/**
 * Public/private key pair entity class
 *
 * @author kongyu
 * @create 2018-02-26 14:22
 */
@Setter
@Getter
public class KeyPair extends BaseSerializer {
    private String priKey;
    private String pubKey;

    public KeyPair() {
    }

    public boolean valid() {
        if (StringUtils.isEmpty(priKey)) {
            return false;
        }
        if (StringUtils.isEmpty(pubKey)) {
            return false;
        }
        return true;
    }

    public KeyPair(String privateKey, String publicKey) {
        priKey = privateKey;
        pubKey = publicKey;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof KeyPair)) {
            return false;
        }

        KeyPair keyPair = (KeyPair) o;

        if (priKey != null ? !priKey.equals(keyPair.priKey) : keyPair.priKey != null) {
            return false;
        }
        return pubKey != null ? pubKey.equals(keyPair.pubKey) : keyPair.pubKey == null;
    }

    @Override
    public int hashCode() {
        int result = priKey != null ? priKey.hashCode() : 0;
        result = 31 * result + (pubKey != null ? pubKey.hashCode() : 0);
        return result;
    }

    public String getAddress() {
        return ECKey.pubKey2Base58Address(pubKey);
    }
}
