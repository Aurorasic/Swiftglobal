package com.higgsblock.global.chain.app.blockchain;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.alibaba.fastjson.annotation.JSONType;
import com.higgsblock.global.chain.common.entity.BaseSerializer;
import com.higgsblock.global.chain.common.utils.Money;
import com.higgsblock.global.chain.crypto.ECKey;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.commons.lang.StringUtils;

/**
 * Paired public key and signature
 *
 * @author baizhengwen
 * @date 2018 /2/22
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@JSONType(includes = {"pubKey", "signature"})
public class SignaturePair extends BaseSerializer {
    /**
     * The Pub key.
     */
    private String pubKey;
    /**
     * The Signature.
     */
    private String signature;

    /**
     * Valid boolean.
     *
     * @return the boolean
     */
    public boolean valid() {
        if (StringUtils.isEmpty(signature)) {
            return false;
        }

        if (StringUtils.isEmpty(pubKey)) {
            return false;
        }
        return true;
    }

    /**
     * Gets address.
     *
     * @return the address
     */
    public String getAddress() {
        String address = ECKey.pubKey2Base58Address(pubKey);
        return address;
    }
}
