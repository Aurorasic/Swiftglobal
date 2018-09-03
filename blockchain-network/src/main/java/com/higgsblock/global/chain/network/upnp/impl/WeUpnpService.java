package com.higgsblock.global.chain.network.upnp.impl;

import com.higgsblock.global.chain.network.upnp.IUpnpService;
import com.higgsblock.global.chain.network.upnp.exception.NotDiscoverUpnpGatewayException;
import com.higgsblock.global.chain.network.upnp.exception.UpnpException;
import com.higgsblock.global.chain.network.upnp.model.PortMappingInfo;
import com.higgsblock.global.chain.network.enums.ProtocolEnum;
import com.higgsblock.global.chain.network.upnp.model.UpnpConstant;
import org.bitlet.weupnp.GatewayDevice;
import org.bitlet.weupnp.GatewayDiscover;
import org.bitlet.weupnp.PortMappingEntry;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * The type We upnp service.
 * Note: Based on the open source project: https://github.com/bitletorg/weupnp
 *
 * @author yanghuadong
 * @date 2018 -05-16
 */
public class WeUpnpService implements IUpnpService {

    /**
     * The Gateway discover.
     */
    private final GatewayDiscover gatewayDiscover = new GatewayDiscover();

    /**
     * The Valid gateway.
     */
    private GatewayDevice device;

    /**
     * The Is discovered.
     */
    private boolean isDiscovered = false;

    /**
     * Discover boolean.
     *
     * @param milliseconds the milliseconds
     * @return the boolean
     */
    @Override
    public boolean discover(int milliseconds) throws NotDiscoverUpnpGatewayException {
        if (milliseconds > 0) {
            this.gatewayDiscover.setTimeout(milliseconds);
        }

        return this.discover();
    }

    /**
     * Discover boolean.
     *
     * @return the boolean
     */
    @Override
    public boolean discover() throws NotDiscoverUpnpGatewayException {
        if (this.isDiscovered) {
            return this.isDiscovered;
        }

        try {
            Map<InetAddress, GatewayDevice> map = this.gatewayDiscover.discover();
            this.isDiscovered = null != map && 0 < map.size();
            this.device = this.gatewayDiscover.getValidGateway();
        } catch (IOException | SAXException | ParserConfigurationException e) {
            this.isDiscovered = false;
            throw new NotDiscoverUpnpGatewayException("discover gateway device error:" + e.getMessage(), e);
        }

        this.check();
        return this.isDiscovered;
    }

    /**
     * Is discovered boolean.
     *
     * @return the boolean
     */
    @Override
    public boolean isDiscovered() {
        return this.isDiscovered;
    }

    /**
     * Gets port mapping info.
     *
     * @param externalPort the external port
     * @param protocol     the protocol
     * @return the port mapping info
     * @throws NotDiscoverUpnpGatewayException the not discover upnp gateway exception
     * @throws UpnpException                   the upnp exception
     */
    @Override
    public PortMappingInfo getPortMappingInfo(int externalPort, ProtocolEnum protocol) throws NotDiscoverUpnpGatewayException, UpnpException {
        this.check();

        PortMappingEntry portMappingEntry = new PortMappingEntry();
        PortMappingInfo portMappingInfo = null;
        try {
            boolean isGet = this.device.getSpecificPortMappingEntry(externalPort, protocol.getName(), portMappingEntry);
            if (isGet) {
                portMappingInfo = this.mapEntryToInfo(portMappingEntry);
            }
        } catch (IOException | SAXException e) {
            throw new UpnpException(externalPort, protocol.getName(), "get port mapping info error:" + e.getMessage(), e);
        }

        return portMappingInfo;
    }

    /**
     * Gets port mapping count.
     *
     * @return the port mapping count
     */
    @Override
    public Integer getPortMappingCount() throws NotDiscoverUpnpGatewayException, UpnpException {
        return this.getAllMappingInfos().size();
    }

    /**
     * Gets port mapping info by index.
     *
     * @param index the index
     * @return the port mapping info by index
     */
    @Override
    public PortMappingInfo getPortMappingInfoByIndex(int index) throws NotDiscoverUpnpGatewayException, UpnpException {
        this.check();
        PortMappingEntry portMappingEntry = new PortMappingEntry();
        PortMappingInfo portMappingInfo = null;
        try {
            if (this.device.getGenericPortMappingEntry(index, portMappingEntry)) {
                portMappingInfo = this.mapEntryToInfo(portMappingEntry);
            }
        } catch (IOException | SAXException e) {
            throw new UpnpException("get port mapping info by index error:" + e.getMessage(), e);
        }

        return portMappingInfo;
    }

    /**
     * Gets all mapping infos.
     *
     * @return the all mapping infos
     */
    @Override
    public List<PortMappingInfo> getAllMappingInfos() throws NotDiscoverUpnpGatewayException, UpnpException {
        this.check();
        List<PortMappingInfo> list = new ArrayList<>();
        int index = 0;
        while (index < UpnpConstant.MAX_MAPPING_COUNT) {
            PortMappingInfo portMappingInfo = this.getPortMappingInfoByIndex(index);
            if (null != portMappingInfo) {
                list.add(portMappingInfo);
                index++;
            } else {
                break;
            }
        }

        return list;
    }

    /**
     * Gets internal host address.
     *
     * @return the internal host address
     * @throws NotDiscoverUpnpGatewayException the not discover upnp gateway exception
     */
    @Override
    public String getInternalHostAddress() throws NotDiscoverUpnpGatewayException {
        this.check();

        return this.device.getLocalAddress().getHostAddress();
    }

    /**
     * Gets external ip address.
     *
     * @return the external ip address
     * @throws NotDiscoverUpnpGatewayException the not discover upnp gateway exception
     * @throws UpnpException                   the upnp exception
     */
    @Override
    public String getExternalIPAddress() throws NotDiscoverUpnpGatewayException, UpnpException {
        this.check();

        try {
            return this.device.getExternalIPAddress();
        } catch (IOException | SAXException e) {
            throw new UpnpException("get external IP address error:" + e.getMessage(), e);
        }
    }

    /**
     * Add port mapping boolean.
     *
     * @param portMappingInfo the port mapping info
     * @return the boolean
     * @throws NotDiscoverUpnpGatewayException the not discover upnp gateway exception
     * @throws UpnpException                   the upnp exception
     */
    @Override
    public boolean addPortMapping(final PortMappingInfo portMappingInfo) throws NotDiscoverUpnpGatewayException, UpnpException {
        this.check();

        Objects.requireNonNull(portMappingInfo, "portMappingInfo");
        PortMappingInfo.checkPortRange(portMappingInfo.getExternalPort(), "external");
        PortMappingInfo.checkPortRange(portMappingInfo.getInternalPort(), "internal");
        Objects.requireNonNull(portMappingInfo.getProtocol(), "protocol");
        Objects.requireNonNull(portMappingInfo.getName(), "description");

        try {
            return this.device.addPortMapping(portMappingInfo.getExternalPort(), portMappingInfo.getInternalPort(), portMappingInfo.getInternalClient(), portMappingInfo.getProtocol().getName(), portMappingInfo.getName());
        } catch (IOException | SAXException e) {
            throw new UpnpException(portMappingInfo.getExternalPort(), portMappingInfo.getProtocol().getName(), "addOrUpdate port mapping error:" + e.getMessage(), e);
        }
    }

    /**
     * Delete port mapping boolean.
     *
     * @param externalPort the external port
     * @param protocol     the protocol
     * @return the boolean
     * @throws NotDiscoverUpnpGatewayException the not discover upnp gateway exception
     * @throws UpnpException                   the upnp exception
     */
    @Override
    public boolean deletePortMapping(int externalPort, ProtocolEnum protocol) throws NotDiscoverUpnpGatewayException, UpnpException {
        this.check();

        PortMappingInfo.checkPortRange(externalPort, "external");
        Objects.requireNonNull(protocol, "protocol");

        try {
            return this.device.deletePortMapping(externalPort, protocol.getName());
        } catch (IOException | SAXException e) {
            throw new UpnpException(externalPort, protocol.getName(), "delete port mapping error:" + e.getMessage(), e);
        }
    }

    /**
     * Map entry to info port mapping info.
     *
     * @param entry the entry
     * @return the port mapping info
     */
    private PortMappingInfo mapEntryToInfo(PortMappingEntry entry) {
        return PortMappingInfo.builder()
                .internalPort(entry.getInternalPort())
                .externalPort(entry.getExternalPort())
                .remoteHost(entry.getRemoteHost())
                .internalClient(entry.getInternalClient())
                .protocol(entry.getProtocol())
                .enabled(entry.getEnabled())
                .name(entry.getPortMappingDescription())
                .build();
    }

    /**
     * Check.
     *
     * @throws NotDiscoverUpnpGatewayException the not discover upnp gateway exception
     */
    private void check() throws NotDiscoverUpnpGatewayException {
        if (!this.isDiscovered || null == this.device) {
            throw new NotDiscoverUpnpGatewayException("not discover a valid gatewat device,please make sure the device has opened upnp function");
        }
    }
}