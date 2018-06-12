package com.higgsblock.global.chain.network.enums;

/**
 * The enum Protocol enum.
 *
 * @author yanghuadong
 * @date 2018 -05-21
 */
public enum ProtocolEnum {
    /**
     * Tcp protocol enum.
     */
    TCP("TCP"),
    /**
     * Udp protocol enum.
     */
    UDP("UDP");

    /**
     * The Name.
     */
    final String name;

    /**
     * Instantiates a new Protocol enum.
     *
     * @param name the name
     */
    ProtocolEnum(final String name) {
        this.name = name;
    }

    /**
     * Gets protocol.
     *
     * @param name the name
     * @return the protocol
     */
    public static ProtocolEnum getProtocol(final String name) {
        if (null == name) {
            return null;
        }

        for (ProtocolEnum item : ProtocolEnum.values()) {
            if (name.equals(item.getName())) {
                return item;
            }
        }

        return null;
    }

    /**
     * Gets name.
     *
     * @return the name
     */
    public String getName() {
        return name;
    }
}