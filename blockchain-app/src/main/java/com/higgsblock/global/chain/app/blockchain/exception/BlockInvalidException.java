package com.higgsblock.global.chain.app.blockchain.exception;

/**
 * @author yuguojia
 * @date 2018/07/30
 **/
public class BlockInvalidException extends RuntimeException {
    public BlockInvalidException(String s) {
        super(s);
    }

    public BlockInvalidException(String s, Throwable throwable) {
        super(s, throwable);
    }
}