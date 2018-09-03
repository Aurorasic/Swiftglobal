package com.higgsblock.global.chain.app.api.vo;

import lombok.Data;

/**
 * @author baizhengwen
 * @date 2018/7/16
 */
@Data
public class PeerVO {

    private String address;
    private String pubKey;
    private String ip;
    private Integer socketServerPort;
    private Integer httpServerPort;

}
