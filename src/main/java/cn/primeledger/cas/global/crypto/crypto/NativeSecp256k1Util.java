package cn.primeledger.cas.global.crypto.crypto;

import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static jdk.nashorn.internal.runtime.regexp.joni.Config.log;

/**
 * @author kongyu
 * @create 2018-02-24 11:24
 */
@Slf4j
public class NativeSecp256k1Util {
    private static final Logger log = LoggerFactory.getLogger(NativeSecp256k1Util.class);

    public static void assertEquals(int val, int val2, String message) throws AssertFailException {
        if (val != val2) {
            throw new AssertFailException("FAIL: " + message);
        }
    }

    public static void assertEquals(boolean val, boolean val2, String message) throws AssertFailException {
        if (val != val2) {
            throw new AssertFailException("FAIL: " + message);
        } else {
            log.debug("PASS: " + message);
        }
    }

    public static void assertEquals(String val, String val2, String message) throws AssertFailException {
        if (!val.equals(val2)) {
            throw new AssertFailException("FAIL: " + message);
        } else {
            log.debug("PASS: " + message);
        }
    }

    public static class AssertFailException extends Exception {
        public AssertFailException(String message) {
            super(message);
        }
    }
}
