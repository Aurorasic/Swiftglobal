package cn.primeledger.cas.global.p2p.message;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * @author yuanjiantao
 * @date Created in 3/2/2018
 */
@Data
@AllArgsConstructor
public class HelloAckWraper {

    private String ip;
    private int port;
    private long timestamp;
}
