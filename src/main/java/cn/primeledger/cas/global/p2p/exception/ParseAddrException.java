package cn.primeledger.cas.global.p2p.exception;

/**
 * An exception thrown when the returned value cannot be parsed.
 *
 * @author  zhao xiaogang
 * */
public class ParseAddrException extends Exception {
    public ParseAddrException() {
    }

    public ParseAddrException(String message) {
        super(message);
    }

    public ParseAddrException(String message, Throwable cause) {
        super(message, cause);
    }

    public ParseAddrException(Throwable cause) {
        super(cause);
    }

    public ParseAddrException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
