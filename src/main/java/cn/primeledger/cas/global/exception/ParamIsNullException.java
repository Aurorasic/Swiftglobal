package cn.primeledger.cas.global.exception;

public class ParamIsNullException extends IllegalArgumentException {
    public ParamIsNullException() {
        super();
    }

    public ParamIsNullException(String message) {
        super(message);
    }
}
