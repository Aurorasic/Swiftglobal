package com.higgsblock.global.chain.network.upnp;

import com.higgsblock.global.chain.network.config.PeerConfig;
import com.higgsblock.global.chain.network.upnp.exception.NotDiscoverUpnpGatewayException;
import com.higgsblock.global.chain.network.upnp.exception.UpnpException;
import com.higgsblock.global.chain.network.upnp.model.PortMappingInfo;
import com.higgsblock.global.chain.network.upnp.model.UpnpConstant;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * The type Upnp manager.
 *
 * @author yanghuadong
 * @date 2018 -05-21
 */
@Service("upnpManager")
@Slf4j
public class UpnpManager {
    /**
     * The App config.
     */
    @Autowired
    private PeerConfig peerConfig;

    /**
     * Get socket port int.
     *
     * @return the int
     */
    public int getSocketMappingPort() {
        final int port = peerConfig.getSocketPort();
        UpnpDiscover discover = this.getDiscover();
        if (null == discover) {
            return port;
        }

        int mapPort = port;
        try {
            String internalIp = discover.getUpnpService().getInternalHostAddress();
            List<PortMappingInfo> allPortMappings = discover.getUpnpService().getAllMappingInfos();
            PortMappingInfo socketPortMapping = allPortMappings.stream()
                    .filter(p -> p.getExternalPort() == port
                            && StringUtils.equals(internalIp, p.getInternalClient())
                            && UpnpConstant.SOCKET_PORT_MAPPING_NAME.equals(p.getName()))
                    .findFirst().orElse(null);
            if (null != socketPortMapping) {
                mapPort = socketPortMapping.getExternalPort();
            } else {
                mapPort = addMappingSocketPort(discover, port);
            }
        } catch (NotDiscoverUpnpGatewayException | UpnpException e) {
            LOGGER.error(e.getMessage(), e);
        }

        return mapPort;
    }

    /**
     * Gets http port.
     *
     * @return the http port
     */
    public int getHttpMappingPort() {
        final int port = peerConfig.getHttpPort();
        UpnpDiscover discover = this.getDiscover();
        if (null == discover) {
            return port;
        }

        int mapPort = port;
        try {
            String internalIp = discover.getUpnpService().getInternalHostAddress();
            List<PortMappingInfo> allPortMappings = discover.getUpnpService().getAllMappingInfos();
            PortMappingInfo portMappingInfo = allPortMappings.stream()
                    .filter(p -> p.getExternalPort() == port
                            && StringUtils.equals(internalIp, p.getInternalClient())
                            && UpnpConstant.HTTP_PORT_MAPPING_NAME.equals(p.getName()))
                    .findFirst().orElse(null);
            if (null != portMappingInfo) {
                mapPort = portMappingInfo.getExternalPort();
            } else {
                mapPort = addMappingHttpPort(discover, port);
            }
        } catch (NotDiscoverUpnpGatewayException | UpnpException e) {
            LOGGER.error(e.getMessage(), e);
        }

        return mapPort;
    }

    /**
     * Gets discover.
     *
     * @return the discover
     */
    private UpnpDiscover getDiscover() {
        UpnpDiscover discover = new UpnpDiscover();
        try {
            discover.getUpnpService().discover();
        } catch (NotDiscoverUpnpGatewayException e) {
            LOGGER.error(e.getMessage(), e);
            discover = null;
        }

        return discover;
    }

    /**
     * Gets mapping socket port.
     *
     * @param discover the discover
     * @param initPort the init port
     * @return the mapping socket port
     */
    private int addMappingSocketPort(UpnpDiscover discover, int initPort) {
        if (null == discover) {
            return initPort;
        }

        int port = initPort;
        try {
            PortMappingInfo mappingInfo = discover.autoMapPort(initPort, peerConfig.getSocketPort(), UpnpConstant.SOCKET_PORT_MAPPING_NAME);
            if (null != mappingInfo) {
                port = mappingInfo.getExternalPort();
                LOGGER.info("upnp socket port mapping info successful:{}", mappingInfo);
            }
        } catch (NotDiscoverUpnpGatewayException | UpnpException e) {
            LOGGER.error(e.getMessage(), e);
        }

        return port;
    }

    /**
     * Gets mapping http port.
     *
     * @param discover the discover
     * @param initPort the init port
     * @return the mapping http port
     */
    private int addMappingHttpPort(UpnpDiscover discover, int initPort) {
        if (null == discover) {
            return initPort;
        }

        int port = initPort;
        try {
            PortMappingInfo mappingInfo = discover.autoMapPort(initPort, peerConfig.getHttpPort(), UpnpConstant.HTTP_PORT_MAPPING_NAME);
            if (null != mappingInfo) {
                port = mappingInfo.getExternalPort();
                LOGGER.info("upnp http port mapping info successful: {}", mappingInfo);
            }
        } catch (NotDiscoverUpnpGatewayException | UpnpException e) {
            LOGGER.error(e.getMessage(), e);
        }

        return port;
    }
}