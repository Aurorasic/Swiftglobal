package cn.primeledger.cas.global.p2p;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author yuanjiantao
 * @date Created on 3/4/2018
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class RegistryPeer {

    private String pubKey;

    private String ip;

    private int port;
}
