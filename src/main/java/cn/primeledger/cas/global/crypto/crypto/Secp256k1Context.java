package cn.primeledger.cas.global.crypto.crypto;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.security.AccessControlException;

/**
 * @author kongyu
 * @create 2018-02-24 14:34
 */
public class Secp256k1Context {
    /**
     * true if the library is loaded
     */
    private static final boolean ENABLED;
    /**
     * ref to pointer to context obj
     */
    private static final long CONTEXT;

    private static final Logger log = LoggerFactory.getLogger(Secp256k1Context.class);

    // static initializer
    static {
        boolean isEnabled = true;
        long contextRef = -1;
        try {
            System.loadLibrary("secp256k1");
            contextRef = secp256k1_init_context();
        } catch (UnsatisfiedLinkError e) {
            log.info(e.toString());
            isEnabled = false;
        } catch (AccessControlException e) {
            log.debug(e.toString());
            isEnabled = false;
        }
        ENABLED = isEnabled;
        CONTEXT = contextRef;
    }

    public static boolean isEnabled() {
        return ENABLED;
    }

    public static long getContext() {
        if (!ENABLED) {
            return -1;
        }
        return CONTEXT;
    }

    private static native long secp256k1_init_context();
}
