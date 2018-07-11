package com.higgsblock.global.chain.crypto.exception;

/**
 * Address format exception class
 *
 * @author kongyu
 * @create 2018-02-24 10:56
 */
public class AddressFormatException extends IllegalArgumentException {
    public AddressFormatException() {
        super();
    }

    public AddressFormatException(String message) {
        super(message);
    }
}
