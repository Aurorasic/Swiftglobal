package com.higgsblock.global.chain.network.utils;

import org.apache.commons.lang3.StringUtils;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

/**
 * The type Network util.
 *
 * @author yanghuadong
 * @date 2018 -05-24
 */
public final class NetworkUtil {

    /**
     * The constant INT_IP_PREFIX_10.
     */
    private static final String INT_IP_PREFIX_10 = "10.";

    /**
     * The constant INT_IP_PREFIX_192_168.
     */
    private static final String INT_IP_PREFIX_192_168 = "192.168.";

    /**
     * The constant MIN_IP_SUB.
     */
    private static final int MIN_IP_SUB = 17216;

    /**
     * The constant MAX_IP_SUB.
     */
    private static final int MAX_IP_SUB = 17231;

    /**
     * Instantiates a new Network util.
     */
    private NetworkUtil() {
    }

    /**
     * Get local private IP
     *
     * @return the local ip
     */
    public static String getLocalIp() {
        List<String> ips = getLocalIps();
        for (String ip : ips) {
            if (isIntranetIp(ip)) {
                return ip;
            }
        }

        return "";
    }

    /**
     * Gets ip by name.
     *
     * @param hostName the host name
     * @return the ip by name
     */
    public static String getIpByName(String hostName) {
        String ip;
        try {
            InetAddress inetAddress = InetAddress.getByName(hostName);
            ip = inetAddress.getHostAddress();
        } catch (UnknownHostException e) {
            ip = getLocalIp();
        }

        return ip;
    }

    /**
     * Check if belongs to the private IP address
     * In the tcp/ip protocol, it specially keeps three types of IP address as private address. The types as below:
     * 10.0.0.0/8: 10.0.0.0～10.255.255.255
     * 172.16.0.0/12: 172.16.0.0～172.31.255.255
     * 192.168.0.0/16: 192.168.0.0～192.168.255.255
     *
     * @param ip the ip
     * @return the boolean
     */
    private static boolean isIntranetIp(String ip) {
        try {
            if (ip.startsWith(INT_IP_PREFIX_10) || ip.startsWith(INT_IP_PREFIX_192_168)) {
                return true;
            }
            // 172.16.x.x～172.31.x.x
            String[] ns = ip.split("\\.");
            int ipSub = Integer.valueOf(ns[0] + ns[1]);
            if (ipSub >= MIN_IP_SUB && ipSub <= MAX_IP_SUB) {
                return true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;
    }

    /**
     * Gets local ips.
     *
     * @return the local ips
     */
    private static List<String> getLocalIps() {
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