package com.higgsblock.global.chain.network.upnp;

import com.higgsblock.global.chain.network.upnp.exception.NotDiscoverUpnpGatewayException;
import com.higgsblock.global.chain.network.upnp.exception.UpnpException;
import com.higgsblock.global.chain.network.upnp.impl.WeUpnpService;
import com.higgsblock.global.chain.network.upnp.model.PortMappingInfo;
import com.higgsblock.global.chain.network.upnp.model.UpnpConstant;

import java.util.Objects;

/**
 * The type Upnp discover.
 *
 * @author yanghuadong
 * @date 2018 -05-17
 */
public class UpnpDiscover {
    /**
     * The Upnp service.
     */
    private IUpnpService upnpService;

    /**
     * Instantiates a new Upnp discover. Default WeUpnpService
     */
    public UpnpDiscover() {
        this.upnpService = new WeUpnpService();
    }

    /**
     * Gets upnp service.
     *
     * @return the upnp service
     */
    public IUpnpService getUpnpService() {
        return upnpService;
    }

    /**
     * Add port mapping port mapping info. Until find a valid port.
     *
     * @param externalPort the external port
     * @param internalPort the internal port
     * @param name         the name
     * @return the port mapping info
     * @throws NotDiscoverUpnpGatewayException the not discover upnp gateway exception
     * @throws UpnpException                   the upnp exception
     */
    public PortMappingInfo autoMapPort(final int externalPort, final int internalPort, final String name) throws NotDiscoverUpnpGatewayException, UpnpException {
        // 1.check the parameter
        PortMappingInfo.checkPortRange(externalPort, name);
        PortMappingInfo.checkPortRange(internalPort, name);
        Objects.requireNonNull(name, "port mapping name can't be null");

        // 2.discover the upnp device
        this.upnpService.discover();

        int tempPort = externalPort;
        boolean isSuccess = false;
        PortMappingInfo addPortMappingInfo = null;

        do {
            // 3.find the port's mapping info
            PortMappingInfo mappingInfo = this.upnpService.getPortMappingInfo(tempPort, UpnpConstant.DEFAULT_PROTOCOL);
            if (null == mappingInfo) {
                // 4.if not find, then addOrUpdate port mapping info
                addPortMappingInfo = PortMappingInfo.builder()
                        .externalPort(tempPort)
                        .internalPort(internalPort)
                        .protocol(UpnpConstant.DEFAULT_PROTOCOL)
                        .name(name)
                        .enabled(true)
                        .internalClient(this.upnpService.getInternalHostAddress())
                        .build();
                isSuccess = this.upnpService.addPortMapping(addPortMappingInfo);
                if (!isSuccess) {
                    tempPort++;
                }
            } else {
                // 3.if the port mapping info belongs to higgs global
                if (name.equals(mappingInfo.getName())) {
                    // 4.if the port is equals to the current port
                    if (this.upnpService.getInternalHostAddress().equals(mappingInfo.getInternalClient())
                            && internalPort == mappingInfo.getInternalPort()) {
                        isSuccess = true;
                        addPortMappingInfo = mappingInfo;
                        break;
                    } else {
                        // firstly delete the current port mapping info
                        this.upnpService.deletePortMapping(mappingInfo.getExternalPort(), UpnpConstant.DEFAULT_PROTOCOL);
                        // then addOrUpdate port mapping info(automatically load newly internal host address)
                        continue;
                    }
                } else {
                    //  4.else increase the port number to retry
                    tempPort++;
                }
            }
        } while (!isSuccess && tempPort <= UpnpConstant.MAX_PORT);

        return isSuccess ? addPortMappingInfo : null;
    }
}