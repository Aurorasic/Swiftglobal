package com.higgsblock.global.chain.app.api.vo;

import lombok.Data;

@Data
public class PeerVO {

    private String address;
    private String pubKey;
    private String ip;
    private int socketServerPort;
    private int httpServerPort;

}
