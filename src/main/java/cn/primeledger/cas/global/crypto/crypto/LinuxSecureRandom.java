package cn.primeledger.cas.global.crypto.crypto;

import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.security.Provider;
import java.security.SecureRandomSpi;
import java.security.Security;

/**
 * @author kongyu
 * @create 2018-02-23 15:24
 */
@Slf4j
public class LinuxSecureRandom extends SecureRandomSpi {

    private static final Logger log = LoggerFactory.getLogger(LinuxSecureRandom.class);
    private static final FileInputStream U_RANDOM;

    static {
        try {
            File file = new File("/dev/urandom");
            U_RANDOM = new FileInputStream(file);
            if (U_RANDOM.read() == -1) {
                throw new RuntimeException("/dev/urandom not readable?");
            }
            int position = Security.insertProviderAt(new LinuxSecureRandomProvider(), 1);

            if (position != -1) {
                log.info("Secure randomness will be read from {} only.", file);
            } else {
                log.info("Randomness is already secure.");
            }
        } catch (FileNotFoundException e) {
            log.error("/dev/urandom does not appear to exist or is not openable");
            throw new RuntimeException(e);
        } catch (IOException e) {
            log.error("/dev/urandom does not appear to be readable");
            throw new RuntimeException(e);
        }
    }

    private final DataInputStream dis;

    public LinuxSecureRandom() {
        dis = new DataInputStream(U_RANDOM);
    }

    @Override
    protected void engineSetSeed(byte[] bytes) {
    }

    @Override
    protected void engineNextBytes(byte[] bytes) {
        try {
            // This will block until all the bytes can be
            dis.readFully(bytes);
            // read.
        } catch (IOException e) {
            // Fatal error. Do not attempt to
            throw new RuntimeException(e);
            // recover from this.
        }
    }

    @Override
    protected byte[] engineGenerateSeed(int i) {
        byte[] bits = new byte[i];
        engineNextBytes(bits);
        return bits;
    }

    private static class LinuxSecureRandomProvider extends Provider {
        public LinuxSecureRandomProvider() {
            super("LinuxSecureRandom", 1.0, "A Linux specific random number provider that uses /dev/urandom");
            put("SecureRandom.LinuxSecureRandom", LinuxSecureRandom.class.getName());
        }
    }
}
