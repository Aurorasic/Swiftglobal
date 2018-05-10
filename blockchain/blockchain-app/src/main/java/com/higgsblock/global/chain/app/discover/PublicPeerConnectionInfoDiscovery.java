package com.higgsblock.global.chain.app.discover;

import com.higgsblock.global.chain.app.config.AppConfig;
import com.higgsblock.global.chain.network.http.HttpClient;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author baizhengwen
 * @date 2018-4-11
 */
@Slf4j
@Component
public class PublicPeerConnectionInfoDiscovery implements IPeerConnectionInfoDiscovery {

    @Autowired
    private AppConfig config;

    @Override
    public String getIp() {
        String pbNetworkIP = null;
        String ip = null;
        int port = 80;
        ip = "icanhazip.com";
        pbNetworkIP = getAutoIP(ip, port);
        if (StringUtils.isEmpty(pbNetworkIP)) {
            ip = "checkip.amazonaws.com";
            pbNetworkIP = getAutoIP(ip, port);
            if (StringUtils.isEmpty(pbNetworkIP)) {
                ip = "api.ipify.org";
                pbNetworkIP = getAutoIP(ip, port);
            }
        }
        return pbNetworkIP;
    }

    @Override
    public int getSocketPort() {
        return config.getSocketServerPort();
    }

    @Override
    public int getHttpPort() {
        return config.getHttpServerPort();
    }

    private String getAutoIP(String ip, int port) {
        String pbNetworkIP = null;
        try {
            IAutoIp api = HttpClient.getApi(ip, port, IAutoIp.class);
            pbNetworkIP = api.getAutoIp().execute().body();
        } catch (IOException e) {
            LOGGER.error("Get public netWork IP error, please check the " + ip, e.getMessage());
        }
        //Check the IP format and scope.
        if (StringUtils.isNotEmpty(pbNetworkIP)) {
            if (isIP(pbNetworkIP)) {
                return pbNetworkIP;
            }
        }
        return pbNetworkIP;
    }

    /**
     * Check the IP format and scope.
     *
     * @param addr: IP address
     */
    private boolean isIP(String addr) {
        if (addr.length() < 7 || addr.length() > 15 || "".equals(addr)) {
            return false;
        }

        String rexp = "^(1\\d{2}|2[0-4]\\d|25[0-5]|[1-9]\\d|[1-9])\\."

                + "(1\\d{2}|2[0-4]\\d|25[0-5]|[1-9]\\d|\\d)\\."

                + "(1\\d{2}|2[0-4]\\d|25[0-5]|[1-9]\\d|\\d)\\."

                + "(1\\d{2}|2[0-4]\\d|25[0-5]|[1-9]\\d|\\d)$";

        Pattern pat = Pattern.compile(rexp);
        Matcher mat = pat.matcher(addr);

        return mat.find();
    }
}


