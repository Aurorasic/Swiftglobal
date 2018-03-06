package cn.primeledger.cas.global.p2p.message;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * @author yuanjiantao
 * @date Created in 3/5/2018
 */
@Data
@AllArgsConstructor
public class RegisterWrapper {

    private String pubKey;

    private String ip;

    private int port;
}
