package com.higgsblock.global.browser.example.utils;

import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;

/**
 * @author yangshenghong
 * @date 2018-05-24
 */
@Slf4j
public class UrlUtils {
    public static final String SEND_TRANSACTION = "/v1.0.0/transactions/send";

    public static final String GET_TRANSACTIONS_BY_PUB_KEY = "/v1.0.0/transactions/list";

    public static final String GET_TRANSACTIONS_BY_TX_HASH = "/v1.0.0/transactions/info";

    public static final String CHECK_PUB_KEY_IS_MINER = "/v1.0.0/miners/isMiner";

    public static final String GET_MINERS_COUNT = "/v1.0.0/miners/count";

    public static final String GET_MINER_BLOCKS = "/v1.0.0/miners/blocks";

    public static final String GET_BLOCK_BY_BLOCK_HEIGHT = "/v1.0.0/blocks/height";

    public static final String GET_BLOCK = "/v1.0.0/blocks/getBlocks";

    public static final String GET_RECENT_BLOCK_HEADER_LIST = "/v1.0.0/blocks/recentHeaderList";

    public static final String GET_UTXOS = "/v1.0.0/utxos/utxo";

    private static final int MAX_PORT = 65535;

    public static boolean ipPortCheckout(String ip, Integer port) {
        if (StringUtils.isEmpty(ip) || port == null) {
            LOGGER.error("ip or port is empty,ip={},port={}", ip, port);
            return false;
        }
        String regex = "^(1\\d{2}|2[0-4]\\d|25[0-5]|[1-9]\\d|[1-9])\\."
                + "(1\\d{2}|2[0-4]\\d|25[0-5]|[1-9]\\d|\\d)\\."
                + "(1\\d{2}|2[0-4]\\d|25[0-5]|[1-9]\\d|\\d)\\."
                + "(1\\d{2}|2[0-4]\\d|25[0-5]|[1-9]\\d|\\d)$";
        boolean flag = ip.matches(regex);
        if (flag) {
            if (port > 0 && port <= MAX_PORT) {
                return true;
            }
        }
        return false;
    }

    public static String builderUrl(String ip, Integer port, String address) {
        if (port == null || port.intValue() == 0) {
            port = 80;
        }
        return new StringBuffer().append(ip).append(":").append(port).append(address).toString();
    }
}