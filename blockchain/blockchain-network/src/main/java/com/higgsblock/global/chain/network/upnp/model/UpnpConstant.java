package com.higgsblock.global.chain.network.upnp.model;

import com.higgsblock.global.chain.network.enums.ProtocolEnum;

import java.util.Arrays;
import java.util.List;

/**
 * The type Upnp constant.
 *
 * @author yanghuadong
 * @date 2018 -05-21
 */
public final class UpnpConstant {
    /**
     * The constant MIN_PORT. 1
     */
    public static final int MIN_PORT = 1;
    /**
     * The constant MAX_PORT. 65535
     */
    public static final int MAX_PORT = (2 << 15) - 1;

    /**
     * The constant ONE.
     */
    public static final String ONE = "1";
    /**
     * The constant DEFAULT_PROTOCOL.
     */
    public static final ProtocolEnum DEFAULT_PROTOCOL = ProtocolEnum.TCP;

    /**
     * The constant SOCKET_PORT_MAPPING_NAME.
     */
    public static final String SOCKET_PORT_MAPPING_NAME = "higgs global socket";

    /**
     * The constant HTTP_PORT_MAPPING_NAME.
     */
    public static final String HTTP_PORT_MAPPING_NAME = "higgs global http";

    /**
     * The constant MAX_MAPPING_COUNT.
     */
    public static final int MAX_MAPPING_COUNT = 255;

    /**
     * Instantiates a new Upnp constant.
     */
    private UpnpConstant() {
    }
}