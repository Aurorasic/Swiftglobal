package cn.primeledger.cas.global.exception;

import java.io.IOException;

/**
 * KeyPairManagerException
 *
 * @author Su Jiulong
 * @date 2018/2/24
 */
public class KeyPairManagerException extends IOException {
    public KeyPairManagerException() {
        super();
    }

    public KeyPairManagerException(String msg) {
        super(msg);
    }
}
