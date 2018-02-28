package cn.primeledger.cas.global.blockchain;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * Paired public key and signature
 *
 * @author baizhengwen
 * @date Created in 2018/2/22
 */
@Data
@AllArgsConstructor
public class PubKeyAndSignaturePair {
    private String pubKey;
    private String signature;
}
