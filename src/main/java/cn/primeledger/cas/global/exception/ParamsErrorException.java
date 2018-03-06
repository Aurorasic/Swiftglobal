package cn.primeledger.cas.global.exception;

/**
 * @author kongyu
 * @create 2018-02-24 10:56
 */
public class ParamsErrorException extends IllegalArgumentException {
    public ParamsErrorException() {
        super();
    }

    public ParamsErrorException(String message) {
        super(message);
    }
}
