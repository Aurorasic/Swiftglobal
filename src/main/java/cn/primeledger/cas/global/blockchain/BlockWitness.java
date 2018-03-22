package cn.primeledger.cas.global.blockchain;

import cn.primeledger.cas.global.crypto.ECKey;
import cn.primeledger.cas.global.entity.BaseSerializer;
import com.google.common.base.Charsets;
import com.google.common.hash.HashFunction;
import com.google.common.hash.Hashing;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.util.Strings;

/**
 * Paired public key and signature
 *
 * @author baizhengwen
 * @date 2018/2/22
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class BlockWitness extends BaseSerializer {
    private String pubKey;
    private String signature;
    /**
     * the signature comes from witch block.
     * null if it is the miner's pk and sig
     */
    private String blockHash;

    public boolean valid() {
        if (StringUtils.isEmpty(pubKey) ||
                StringUtils.isEmpty(signature)) {
            return false;
        }
        return true;
    }

    public String getAddress() {
        String address = null;

        address = ECKey.pubKey2Base58Address(pubKey);

        return address;
    }

    public String getBlockWitnessHash() {
        HashFunction function = Hashing.sha256();
        StringBuilder builder = new StringBuilder();
        builder.append(null == pubKey ? Strings.EMPTY : pubKey);
        builder.append(null == signature ? Strings.EMPTY : signature);
        builder.append(null == blockHash ? Strings.EMPTY : blockHash);
        return function.hashString(builder, Charsets.UTF_8).toString();
    }
}
