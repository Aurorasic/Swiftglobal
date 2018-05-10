package com.higgsblock.global.chain.crypto.exception;

/**
 * @author kongyu
 * @create 2018-02-24 10:56
 */
public class ECKeyCrypterException extends RuntimeException {
    public ECKeyCrypterException(String s) {
        super(s);
    }

    public ECKeyCrypterException(String s, Throwable throwable) {
        super(s, throwable);
    }
}
