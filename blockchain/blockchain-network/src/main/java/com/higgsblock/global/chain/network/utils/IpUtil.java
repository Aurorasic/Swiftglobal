package com.higgsblock.global.chain.network.utils;

import com.higgsblock.global.chain.network.http.HttpClient;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * The type Ip util.
 *
 * @author yanghuadong
 * @date 2018 -05-24
 */
@Slf4j
public final class IpUtil {

    /**
     * The constant PUBLIC_ID_APIS.
     */
    private static final List<String> PUBLIC_ID_APIS = Arrays.asList("icanhazip.com", "checkip.amazonaws.com", "api.ipify.org");
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
     * The constant ADDR_MIN_LEN.
     */
    private static final int ADDR_MIN_LEN = 7;
    /**
     * The constant ADDR_MAX_LEN.
     */
    private static final int ADDR_MAX_LEN = 15;

    /**
     * Instantiates a new Network util.
     */
    private IpUtil() {
    }

    /**
     * Get public ip string.
     *
     * @return the public ip string
     */
    public static String getPublicIp() {
        String publicIP = null;
        for (String api : PUBLIC_ID_APIS) {
            try {
                publicIP = HttpClient.get(api);
            } catch (Exception e) {
                LOGGER.error(String.format("get public ip error=%s,api=%s", e.getMessage(), api), e);
            }

            if (StringUtils.isNotEmpty(publicIP)) {
                break;
            }
        }

        if (StringUtils.isEmpty(publicIP)) {
            publicIP = getLocalIp();
        }

        LOGGER.info("publicIp={}", publicIP);
        if (isIP(publicIP)) {
            return publicIP;
        }

        return publicIP;
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

        return StringUtils.EMPTY;
    }

    /**
     * Get local ip by name.
     *
     * @param hostName the host name
     * @return the ip by name
     */
    public static String getIpByName(String hostName) {
        String ip = null;
        try {
            InetAddress inetAddress = InetAddress.getByName(hostName);
            ip = inetAddress.getHostAddress();
        } catch (UnknownHostException e) {
            LOGGER.error(e.getMessage(), e);
        }

        if (StringUtils.isEmpty(ip)) {
            ip = getLocalIp();
        }

        LOGGER.info("localIp={}", ip);
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
            LOGGER.error(e.getMessage(), e);
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
            LOGGER.error(e1.getMessage(), e1);
        }

        return ips;
    }

    /**
     * Check the IP format and scope.
     *
     * @param ip : IP address
     * @return the boolean
     */
    private static boolean isIP(String ip) {
        if (StringUtils.isEmpty(ip) || ip.length() < ADDR_MIN_LEN || ip.length() > ADDR_MAX_LEN) {
            return false;
        }

        String rexp = "^(1\\d{2}|2[0-4]\\d|25[0-5]|[1-9]\\d|[1-9])\\."

                + "(1\\d{2}|2[0-4]\\d|25[0-5]|[1-9]\\d|\\d)\\."

                + "(1\\d{2}|2[0-4]\\d|25[0-5]|[1-9]\\d|\\d)\\."

                + "(1\\d{2}|2[0-4]\\d|25[0-5]|[1-9]\\d|\\d)$";

        Pattern pat = Pattern.compile(rexp);
        Matcher mat = pat.matcher(ip);
        return mat.find();
    }
}