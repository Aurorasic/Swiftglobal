package cn.primeledger.cas.global.p2p.message;

import lombok.Data;

import java.io.Serializable;

@Data
public class BizWapper implements Serializable {
    private short type;

    private short version;

    private String data;

}
