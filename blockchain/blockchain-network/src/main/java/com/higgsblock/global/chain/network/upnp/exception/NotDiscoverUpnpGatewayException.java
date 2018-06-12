package com.higgsblock.global.chain.network.upnp.exception;

/**
 * The type Not discover upnp gateway exception.
 *
 * @author yanghuadong
 * @date 2018 -05-21
 */
public class NotDiscoverUpnpGatewayException extends Exception {
    /**
     * Instantiates a new Not discover upnp gateway exception.
     *
     * @param message the message
     */
    public NotDiscoverUpnpGatewayException(String message) {
        super(message);
    }

    /**
     * Instantiates a new Not discover upnp gateway exception.
     *
     * @param message the message
     * @param cause   the cause
     */
    public NotDiscoverUpnpGatewayException(String message, Throwable cause) {
        super(message, cause);
    }
}