package com.higgsblock.global.chain.app.api.outer;

import com.higgsblock.global.chain.app.config.AppConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.servlet.*;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * @author kongyu
 * @date 2018-5-21 16:20
 */
@Component
@Slf4j
@WebFilter(urlPatterns = "/*", filterName = "accessAuthorityFilter", asyncSupported = true)
public class AccessAuthorityFilter implements Filter {

    private static final String SEPARATOR = ";";
    /**
     * Whether you need access
     */
    private boolean isAllowed = false;
    /**
     * The regular expression used to store the list of IP white list after initialization
     */
    private List<String> allowRegexList = new ArrayList<String>();

    @Autowired
    private AppConfig config;

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        //Initialize the whitelist list when the filter is initialized.
        try {
            initAllowList();
        } catch (IOException e) {

        }
    }

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest) servletRequest;
        HttpServletResponse response = (HttpServletResponse) servletResponse;

        String remoteAddr = request.getRemoteAddr();

        if (null == remoteAddr || "".equals(remoteAddr.trim())) {
            throw new RuntimeException("ip address is empty,access denied!");
        }

        //禁止所有http访问
        if (!isAllowed) {
            filterChain.doFilter(request, response);
            return;
        }

        if (null == allowRegexList || allowRegexList.size() == 0) {
            //还需要校验口令
            response.sendError(403, "HASH_NO_PRIVILEGE！");
            return;
        }

        if (!checkIp(remoteAddr)) {
            response.sendError(403, "HASH_NO_PRIVILEGE！");
            return;
        }

        filterChain.doFilter(request, response);
        return;
    }

    @Override
    public void destroy() {

    }

    public void initAllowList() throws IOException {
        isAllowed = config.isAccessIsAllowed();
        //Get the IP configured in three configurations.
        String allowIP = config.getAccessAllowIp();
        String allowIPRange = config.getAccessAllowIpRange();
        String allowIPWildcard = config.getAccessAllowIpWildCard();

        //The IP white list of three ways to configure the user is formatted.
        if (!validate(allowIP, allowIPRange, allowIPWildcard)) {
            throw new RuntimeException("IP the whitelist format defines exceptions!");
        }

        //Parse the IP address that is configured in the first way and add it to the collection of IP whitelists.
        if (null != allowIP && !"".equals(allowIP.trim())) {
            String[] address = allowIP.split(",|;");

            if (null != address && address.length > 0) {
                for (String ip : address) {
                    allowRegexList.add(ip);
                }
            }
        }

        //Parse the IP address configured in the second way and add it to the collection of IP whitelists.
        if (null != allowIPRange && !"".equals(allowIPRange.trim())) {
            String[] addressRanges = allowIPRange.split(",|;");

            if (null != addressRanges && addressRanges.length > 0) {
                for (String addrRange : addressRanges) {
                    String[] addrParts = addrRange.split("-");

                    if (null != addrParts && addrParts.length > 0 && addrParts.length <= 2) {
                        String from = addrParts[0];
                        String to = addrParts[1];
                        String prefix = from.substring(0, from.lastIndexOf(".") + 1);

                        int start = Integer.parseInt(from.substring(from.lastIndexOf(".") + 1, from.length()));
                        int end = Integer.parseInt(to.substring(to.lastIndexOf(".") + 1, to.length()));

                        for (int i = start; i <= end; i++) {
                            allowRegexList.add(prefix + i);
                        }

                    } else {
                        throw new RuntimeException("The ip list format defines exceptions.");
                    }
                }
            }
        }

        //The IP address configured in the third way is resolved into a regular expression that is added to the collection of IP white lists
        if (null != allowIPWildcard && !"".equals(allowIPWildcard.trim())) {
            String[] address = allowIPWildcard.split(",|;");

            if (null != address && address.length > 0) {
                for (String addr : address) {
                    if (addr.indexOf("*") != -1) {
                        //Replace * with a regular expression that matches the single-end IP address
                        addr = addr.replaceAll("\\*", "(1\\\\d{1,2}|2[0-4]\\\\d|25[0-5]|\\\\d{1,2})");
                        addr = addr.replaceAll("\\.", "\\\\.");
                        allowRegexList.add(addr);
                    } else {
                        throw new RuntimeException("The ip whitelist format defines exceptions.!");
                    }
                }
            }
        }
    }

    public boolean validate(String allowIP, String allowIPRange, String allowIPWildcard) {

        //Match the regular of each segment of the IP address.
        String regx = "(1\\d{1,2}|2[0-4]\\d|25[0-5]|\\d{1,2})";
        //To connect the four segments, that is an expression that matches the entire IP address.
        String ipRegx = regx + "\\." + regx + "\\." + regx + "\\." + regx;

        //Verify that the IP white list format configured for the first configuration is correct.
        Pattern pattern = Pattern.compile("(" + ipRegx + ")|(" + ipRegx + "(,|;))*");
        if (!this.validate(allowIP, pattern)) {
            return false;
        }

        //Verify that the IP white list format configured for the second configuration is correct.
        pattern = Pattern.compile("(" + ipRegx + ")\\-(" + ipRegx + ")|" + "((" + ipRegx + ")\\-(" + ipRegx + ")(,|;))*");
        if (!this.validate(allowIPRange, pattern)) {
            return false;
        }

        //Verify that the IP white list format configured for the third configuration is correct.
        pattern = Pattern.compile("(" + regx + "\\." + regx + "\\." + regx + "\\." + "\\*)|" + "(" + regx + "\\." + regx + "\\." + regx + "\\." + "\\*(,|;))*");
        if (!this.validate(allowIPWildcard, pattern)) {
            return false;
        }
        return true;
    }

    /**
     * Verify that the IP list format for user configuration is correct.
     */
    public boolean validate(String allowIP, Pattern pattern) {
        //If it is empty, do not do it.
        if (null != allowIP && !"".equals(allowIP.trim())) {
            StringBuilder sb = new StringBuilder(allowIP);

            //If the user-configured IP configuration is multiple, but does not end with a semicolon, it is given a semicolon.
            if (!SEPARATOR.equals(allowIP)) {
                sb.append(";");
            }

            if (!pattern.matcher(sb).matches()) {
                return false;
            }
        }
        return true;
    }

    /**
     * Check if the user IP is on the white list.
     */
    public boolean checkIp(String remoteAddr) {
        for (String regex : allowRegexList) {
            if (remoteAddr.matches(regex)) {
                return true;
            }
        }
        return false;
    }
}
