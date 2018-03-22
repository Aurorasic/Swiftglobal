package cn.primeledger.cas.global.p2p.event;

import lombok.Data;

@Data
public class SendGetSignEvent {
    private String id;
    long nonce;
}
