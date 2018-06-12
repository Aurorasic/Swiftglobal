package com.higgsblock.global.chain.network.upnp.model;

import com.higgsblock.global.chain.common.entity.BaseSerializer;
import com.higgsblock.global.chain.network.enums.ProtocolEnum;
import org.apache.commons.lang3.StringUtils;

import java.text.MessageFormat;

/**
 * The type Port mapping info.
 *
 * @author yanghuadong
 * @date 2018 -05-21
 */
public class PortMappingInfo extends BaseSerializer {
    /**
     * The Internal port.
     */
    private int internalPort;
    /**
     * The External port.
     */
    private int externalPort;
    /**
     * The Remote host.
     */
    private String remoteHost;
    /**
     * The Internal client.
     */
    private String internalClient;
    /**
     * The Protocol. (i.e. <tt>TCP</tt> or <tt>UDP</tt>)
     */
    private ProtocolEnum protocol;
    /**
     * The Enabled.
     */
    private boolean enabled;
    /**
     * The Description.
     */
    private String name;

    /**
     * Instantiates a new Port mapping info.
     */
    private PortMappingInfo() {

    }

    /**
     * Build port mapping info.
     *
     * @param builder the builder
     * @return the port mapping info
     */
    private PortMappingInfo(PortMappingInfo.Builder builder) {
        this.internalPort = builder.internalPort;
        this.externalPort = builder.externalPort;
        this.remoteHost = builder.remoteHost;
        this.internalClient = builder.internalClient;
        this.protocol = builder.protocol;
        this.enabled = builder.enabled;
        this.name = builder.name;
    }

    /**
     * Builder port mapping info . builder.
     *
     * @return the port mapping info . builder
     */
    public static PortMappingInfo.Builder builder() {
        return new PortMappingInfo.Builder();
    }

    /**
     * Check port range.
     *
     * @param port    the port
     * @param message the message
     */
    public static void checkPortRange(int port, String message) {
        if (port < UpnpConstant.MIN_PORT || port > UpnpConstant.MAX_PORT) {
            String info = MessageFormat.format("{0} port should between {1} and {2},port={3}", message, UpnpConstant.MIN_PORT, UpnpConstant.MAX_PORT, port);
            throw new IllegalArgumentException(info);
        }
    }

    /**
     * Gets internal port.
     *
     * @return the internal port
     */
    public int getInternalPort() {
        return internalPort;
    }

    /**
     * Gets external port.
     *
     * @return the external port
     */
    public int getExternalPort() {
        return externalPort;
    }

    /**
     * Gets remote host.
     *
     * @return the remote host
     */
    public String getRemoteHost() {
        return remoteHost;
    }

    /**
     * Gets internal client.
     *
     * @return the internal client
     */
    public String getInternalClient() {
        return internalClient;
    }

    /**
     * Gets protocol.
     *
     * @return the protocol
     */
    public ProtocolEnum getProtocol() {
        return protocol;
    }

    /**
     * Is enabled boolean.
     *
     * @return the boolean
     */
    public boolean isEnabled() {
        return enabled;
    }

    /**
     * Gets name.
     *
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * The type Builder.
     */
    public static class Builder {
        /**
         * The Internal port.
         */
        private int internalPort;
        /**
         * The External port.
         */
        private int externalPort;
        /**
         * The Remote host.
         */
        private String remoteHost;
        /**
         * The Internal client.
         */
        private String internalClient;
        /**
         * The Protocol. (i.e. <tt>TCP</tt> or <tt>UDP</tt>)
         */
        private ProtocolEnum protocol;
        /**
         * The Enabled.
         */
        private boolean enabled;
        /**
         * The Description.
         */
        private String name;

        /**
         * Internal port builder.
         *
         * @param internalPort the internal port
         * @return the builder
         */
        public Builder internalPort(int internalPort) {
            this.internalPort = internalPort;
            return this;
        }

        /**
         * External port builder.
         *
         * @param externalPort the external port
         * @return the builder
         */
        public Builder externalPort(int externalPort) {
            this.externalPort = externalPort;
            return this;
        }

        /**
         * Remote host builder.
         *
         * @param remoteHost the remote host
         * @return the builder
         */
        public Builder remoteHost(String remoteHost) {
            this.remoteHost = remoteHost;
            return this;
        }

        /**
         * Internal client builder.
         *
         * @param internalClient the internal client
         * @return the builder
         */
        public Builder internalClient(String internalClient) {
            this.internalClient = internalClient;
            return this;
        }

        /**
         * Protocol builder.
         *
         * @param protocol the protocol
         * @return the builder
         */
        public Builder protocol(ProtocolEnum protocol) {
            this.protocol = protocol;
            return this;
        }

        /**
         * Protocol builder.
         *
         * @param protocol the protocol i.e. <tt>TCP</tt> or <tt>UDP</tt>
         * @return the builder
         */
        public Builder protocol(String protocol) {
            this.protocol = ProtocolEnum.getProtocol(protocol);
            return this;
        }

        /**
         * Enabled builder.
         *
         * @param enabled the enabled
         * @return the builder
         */
        public Builder enabled(boolean enabled) {
            this.enabled = enabled;
            return this;
        }

        /**
         * Enabled builder.
         *
         * @param enabled the enabled <tt>"1"</tt> for enabled, <tt>"0"</tt> for disabled
         * @return the builder
         */
        public Builder enabled(String enabled) {
            this.enabled = StringUtils.isEmpty(enabled) ? true : UpnpConstant.ONE.equals(enabled);
            return this;
        }

        /**
         * Description builder.
         *
         * @param name the name
         * @return the builder
         */
        public Builder name(String name) {
            this.name = name;
            return this;
        }

        /**
         * Build port mapping info.
         *
         * @return the port mapping info
         */
        public PortMappingInfo build() {
            return new PortMappingInfo(this);
        }
    }
}