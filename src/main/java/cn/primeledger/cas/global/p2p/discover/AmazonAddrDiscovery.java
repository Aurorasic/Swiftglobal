package cn.primeledger.cas.global.p2p.discover;

import cn.primeledger.cas.global.p2p.exception.ParseAddrException;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.concurrent.Callable;

/**
 * The amazon public ip address discover
 *
 * @author zhao xiaogang
 */

@Slf4j
public class AmazonAddrDiscovery extends BaseAddrDiscovery implements Runnable {
    private final static String HOST = "http://checkip.amazonaws.com";
    private String ip;

    public AmazonAddrDiscovery(String ip) throws MalformedURLException {
        super(new URL(HOST));
        this.ip = ip;
    }

    @Override
    public String parse(String content) throws ParseAddrException {
        String ip = content.trim();

        if (ip.matches("(\\d{1,3}\\.){3}\\d{1,3}")) {
            return ip;
        }

        // do failover
        try {
            return InetAddress.getLocalHost().getHostAddress();
        } catch (Exception e) {
            return InetAddress.getLoopbackAddress().getHostAddress();
        }
    }

    @Override
    public void run() {
        try {
            String newIp = resolveAddress();
            LOGGER.info("Updated public IP address: {}", newIp);
            try {
                if (!ip.equals(newIp) && !InetAddress.getByName(newIp).isSiteLocalAddress()) {
                    LOGGER.info("New public IP address found: {} => {}", ip, newIp);
                    ip = newIp;
                }
            } catch (UnknownHostException e) {
                LOGGER.error("Returned ip is invalid: {}", e.getMessage());
            }
        } catch (IOException e) {
            LOGGER.error("Error for resolving address");
        } catch (ParseAddrException e) {
            LOGGER.error("Error for parsing address");
        }
    }

}
