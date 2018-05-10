package com.higgsblock.global.chain.app.discover;

import com.higgsblock.global.chain.app.config.AppConfig;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

/**
 * @author zhao xiaogang
 * @date 2018/4/11
 */
@Slf4j
@Component
public class LocalPeerConnectionInfoDiscovery implements IPeerConnectionInfoDiscovery {

    @Autowired
    private AppConfig config;

    @Override
    public String getIp() {
        return getLocalIp();
    }

    @Override
    public int getSocketPort() {
        return config.getSocketServerPort();
    }

    @Override
    public int getHttpPort() {
        return config.getHttpServerPort();
    }

    /**
     * Get local private IP
     */
    private String getLocalIp() {
        List<String> ips = getLocalIps();
        for (String ip : ips) {
            if (isIntranetIp(ip)) {
                LOGGER.info("Local address is: {}", ip);
                return ip;
            }
        }
        return "";
    }

    /**
     * Check if belongs to the private IP address
     * In the tcp/ip protocol, it specially keeps three types of IP address as private address. The types as below:
     * 10.0.0.0/8: 10.0.0.0～10.255.255.255
     * 172.16.0.0/12: 172.16.0.0～172.31.255.255
     * 192.168.0.0/16: 192.168.0.0～192.168.255.255
     */
    private boolean isIntranetIp(String ip) {
        try {
            if (ip.startsWith("10.") || ip.startsWith("192.168.")) {
                return true;
            }
            // 172.16.x.x～172.31.x.x
            String[] ns = ip.split("\\.");
            int ipSub = Integer.valueOf(ns[0] + ns[1]);
            if (ipSub >= 17216 && ipSub <= 17231) {
                return true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    private List<String> getLocalIps() {
        List<String> ips = new ArrayList<>();
        try {
            Enumeration<NetworkInterface> enumeration = NetworkInterface.getNetworkInterfaces();
            while (enumeration.hasMoreElements()) {
                NetworkInterface iface = enumeration.nextElement();
                // filters out 127.0.0.1 and inactive interfaces
                if (iface.isLoopback() || !iface.isUp()) {
                    continue;
                }

                Enumeration<InetAddress> inetAddresses = iface.getInetAddresses();
                while (inetAddresses.hasMoreElements()) {
                    String ip = inetAddresses.nextElement().getHostAddress();
                    // Exclude IP/ipv6 address
                    if (ip.contains(":")) {
                        continue;
                    }
                    if (StringUtils.isNotBlank(ip)) {
                        ips.add(ip);
                    }
                }
            }
        } catch (SocketException e1) {
            e1.printStackTrace();
        }

        return ips;
    }
}
