package com.higgsblock.global.chain.network.upnp.exception;

/**
 * The type Upnp exception.
 *
 * @author yanghuadong
 * @date 2018 -05-21
 */
public class UpnpException extends Exception {
    /**
     * The External port.
     */
    private final Integer externalPort;
    /**
     * The Protocol.
     */
    private final String protocol;

    /**
     * Instantiates a new Upnp exception.
     *
     * @param message the message
     */
    public UpnpException(String message) {
        this(null, null, message, null);
    }

    /**
     * Instantiates a new Upnp exception.
     *
     * @param message the message
     * @param cause   the cause
     */
    public UpnpException(String message, Throwable cause) {
        this(null, null, message, cause);
    }

    /**
     * Instantiates a new Upnp exception.
     *
     * @param externalPort the external port
     * @param protocol     the protocol
     * @param message      the message
     * @param cause        the cause
     */
    public UpnpException(Integer externalPort, String protocol, String message, Throwable cause) {
        super(message, cause);
        this.externalPort = externalPort;
        this.protocol = protocol;
    }

    /**
     * Gets external port.
     *
     * @return the external port
     */
    public Integer getExternalPort() {
        return externalPort;
    }

    /**
     * Gets protocol.
     *
     * @return the protocol
     */
    public String getProtocol() {
        return protocol;
    }
}