package cn.primeledger.cas.global.p2p.exception;

/**
 * @author zhao xiaogang
 * */
public class PeerDiscoveryException extends Exception {
    public PeerDiscoveryException() {
    }

    public PeerDiscoveryException(String message) {
        super(message);
    }

    public PeerDiscoveryException(String message, Throwable cause) {
        super(message, cause);
    }

    public PeerDiscoveryException(Throwable cause) {
        super(cause);
    }

    public PeerDiscoveryException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
