package cn.primeledger.cas.global.exception;

public class ECKeyCrypterException extends RuntimeException{
    public ECKeyCrypterException(String s) {
        super(s);
    }

    public ECKeyCrypterException(String s, Throwable throwable) {
        super(s, throwable);
    }
}
