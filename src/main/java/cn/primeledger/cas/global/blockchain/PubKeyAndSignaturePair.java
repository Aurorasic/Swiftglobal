package cn.primeledger.cas.global.blockchain;

import cn.primeledger.cas.global.crypto.ECKey;
import cn.primeledger.cas.global.entity.BaseSerializer;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.commons.lang.StringUtils;

/**
 * Paired public key and signature
 *
 * @author baizhengwen
 * @date 2018/2/22
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class PubKeyAndSignaturePair extends BaseSerializer {
    private String pubKey;
    private String signature;
    /**
     * the signature comes from witch block.
     * null if it is the miner's pk and sig
     */
    private String blockHash;

    public boolean valid(boolean isMiner) {
        if (StringUtils.isEmpty(pubKey) ||
                StringUtils.isEmpty(signature)) {
            return false;
        }
        if (isMiner && StringUtils.isNotEmpty(blockHash)) {
            return false;
        }
        if (!isMiner && StringUtils.isEmpty(blockHash)) {
            return false;
        }
        return true;
    }

    public String getAddress() {
        String address = null;

        address = ECKey.pubKey2Base58Address(pubKey);

        return address;
    }
}
