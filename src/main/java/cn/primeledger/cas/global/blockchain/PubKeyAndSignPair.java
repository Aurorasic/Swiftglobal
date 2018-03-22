package cn.primeledger.cas.global.blockchain;

import cn.primeledger.cas.global.entity.BaseSerializer;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Paired public key and signature
 *
 * @author baizhengwen
 * @date 2018/2/22
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PubKeyAndSignPair extends BaseSerializer {
    private String pubKey;
    private String signature;
}
