package cn.primeledger.cas.global.p2p.exception;

import java.io.IOException;

/**
 * @author zhao xiaogang
 * */
public class NetworkException extends IOException {

    public NetworkException(String message) {
        super(message);
    }
}
