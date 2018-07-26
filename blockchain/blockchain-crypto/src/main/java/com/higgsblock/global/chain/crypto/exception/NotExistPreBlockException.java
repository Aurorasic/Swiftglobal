package com.higgsblock.global.chain.crypto.exception;

/**
 * @author yuguojoia
 * @create 2018-07-26
 */
public class NotExistPreBlockException extends RuntimeException {
    public NotExistPreBlockException(String s) {
        super(s);
    }

    public NotExistPreBlockException(String s, Throwable throwable) {
        super(s, throwable);
    }
}
