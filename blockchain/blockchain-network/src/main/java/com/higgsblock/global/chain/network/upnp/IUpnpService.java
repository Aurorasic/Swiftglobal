package com.higgsblock.global.chain.network.upnp;

import com.higgsblock.global.chain.network.upnp.exception.NotDiscoverUpnpGatewayException;
import com.higgsblock.global.chain.network.upnp.exception.UpnpException;
import com.higgsblock.global.chain.network.upnp.model.PortMappingInfo;
import com.higgsblock.global.chain.network.enums.ProtocolEnum;

import java.util.List;

/**
 * The interface Upnp service.
 *
 * @author yanghuadong
 * @date 2018 -05-21
 */
public interface IUpnpService {
    /**
     * Discover boolean.
     *
     * @param milliseconds the milliseconds
     * @return the boolean
     * @throws NotDiscoverUpnpGatewayException the not discover upnp gateway exception
     */
    boolean discover(int milliseconds) throws NotDiscoverUpnpGatewayException;

    /**
     * Discover boolean.
     *
     * @return the boolean
     * @throws NotDiscoverUpnpGatewayException the not discover upnp gateway exception
     */
    boolean discover() throws NotDiscoverUpnpGatewayException;

    /**
     * Is discovered boolean.
     *
     * @return the boolean
     */
    boolean isDiscovered();

    /**
     * Gets port mapping info.
     *
     * @param externalPort the external port
     * @param protocol     the protocol
     * @return the port mapping info
     * @throws NotDiscoverUpnpGatewayException the not discover upnp gateway exception
     * @throws UpnpException                   the upnp exception
     */
    PortMappingInfo getPortMappingInfo(int externalPort, ProtocolEnum protocol) throws NotDiscoverUpnpGatewayException, UpnpException;

    /**
     * Gets port mapping count.
     *
     * @return the port mapping count
     * @throws NotDiscoverUpnpGatewayException the not discover upnp gateway exception
     * @throws UpnpException                   the upnp exception
     */
    Integer getPortMappingCount() throws NotDiscoverUpnpGatewayException, UpnpException;

    /**
     * Gets port mapping info by index.
     *
     * @param index the index
     * @return the port mapping info by index
     * @throws NotDiscoverUpnpGatewayException the not discover upnp gateway exception
     * @throws UpnpException                   the upnp exception
     */
    PortMappingInfo getPortMappingInfoByIndex(int index) throws NotDiscoverUpnpGatewayException, UpnpException;

    /**
     * Gets all mapping infos.
     *
     * @return the all mapping infos
     * @throws NotDiscoverUpnpGatewayException the not discover upnp gateway exception
     * @throws UpnpException                   the upnp exception
     */
    List<PortMappingInfo> getAllMappingInfos() throws NotDiscoverUpnpGatewayException, UpnpException;

    /**
     * Gets internal host address.
     *
     * @return the internal host address
     * @throws NotDiscoverUpnpGatewayException the not discover upnp gateway exception
     */
    String getInternalHostAddress() throws NotDiscoverUpnpGatewayException;

    /**
     * Gets external ip address.
     *
     * @return the external ip address
     * @throws NotDiscoverUpnpGatewayException the not discover upnp gateway exception
     * @throws UpnpException                   the upnp exception
     */
    String getExternalIPAddress() throws NotDiscoverUpnpGatewayException, UpnpException;

    /**
     * Add port mapping boolean.
     *
     * @param portMappingInfo the port mapping info
     * @return the boolean
     * @throws NotDiscoverUpnpGatewayException the not discover upnp gateway exception
     * @throws UpnpException                   the upnp exception
     */
    boolean addPortMapping(PortMappingInfo portMappingInfo) throws NotDiscoverUpnpGatewayException, UpnpException;

    /**
     * Delete port mapping boolean.
     *
     * @param externalPort the external port
     * @param protocol     the protocol
     * @return the boolean
     * @throws NotDiscoverUpnpGatewayException the not discover upnp gateway exception
     * @throws UpnpException                   the upnp exception
     */
    boolean deletePortMapping(int externalPort, ProtocolEnum protocol) throws NotDiscoverUpnpGatewayException, UpnpException;
}