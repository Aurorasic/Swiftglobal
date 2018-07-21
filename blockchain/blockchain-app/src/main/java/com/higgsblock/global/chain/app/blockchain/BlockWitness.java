package com.higgsblock.global.chain.app.blockchain;

import com.google.common.base.Charsets;
import com.google.common.hash.HashFunction;
import com.google.common.hash.Hashing;
import com.higgsblock.global.chain.common.entity.BaseSerializer;
import com.higgsblock.global.chain.crypto.ECKey;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.util.Strings;

/**
 * Paired public key and signature
 *
 * @author baizhengwen
 * @date 2018 /2/22
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class BlockWitness extends BaseSerializer {
    /**
     * The Pub key.
     */
    private String pubKey;
    /**
     * The Signature.
     */
    private String signature;
    /**
     * the signature comes from witch block.
     * null if it is the miner's pk and sig
     */
    private String blockHash;

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

    /**
     * Gets block witness hash.
     *
     * @return the block witness hash
     */
    public String getBlockWitnessHash() {
        HashFunction function = Hashing.sha256();
        StringBuilder builder = new StringBuilder();
        builder.append(null == pubKey ? Strings.EMPTY : pubKey);
        builder.append(null == signature ? Strings.EMPTY : signature);
        builder.append(null == blockHash ? Strings.EMPTY : blockHash);
        return function.hashString(builder, Charsets.UTF_8).toString();
    }
}
