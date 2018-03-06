package cn.primeledger.cas.global.exception;

/**
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
