package com.higgsblock.global.chain.app.disconver;

import com.higgsblock.global.chain.crypto.ECKey;
import com.higgsblock.global.chain.network.upnp.IUpnpService;
import com.higgsblock.global.chain.network.upnp.exception.NotDiscoverUpnpGatewayException;
import com.higgsblock.global.chain.network.upnp.exception.UpnpException;
import com.higgsblock.global.chain.network.upnp.impl.WeUpnpService;
import com.higgsblock.global.chain.network.upnp.model.PortMappingInfo;
import com.higgsblock.global.chain.network.enums.ProtocolEnum;
import com.higgsblock.global.chain.network.upnp.model.UpnpConstant;
import com.higgsblock.global.chain.network.utils.IpUtil;

/**
 * The type Upnp disconver test.
 *
 * @author yanghuadong
 * @date 2018 -05-21
 */
public class UpnpDisconverTest {
    /**
     * The entry point of application.
     *
     * @param args the input arguments
     */
    public static void main(String[] args) {
        //buildKey();
        upnpTest();
    }

    /**
     * Build key.
     */
    public static void buildKey() {
        for (int i = 0; i < 4; i++) {
            ECKey ecKey = new ECKey();
            System.out.println("peer.priKey=" + ecKey.getKeyPair().getPriKey());
            System.out.println("peer.pubKey=" + ecKey.getKeyPair().getPubKey());
            System.out.println("peer.addr=" + ecKey.toBase58Address());
            System.out.println("----------------------------------------------------------------");
        }
    }

    /**
     * Upnp test.
     */
    public static void upnpTest() {
        IUpnpService upnpService = new WeUpnpService();
        try {
            boolean res = upnpService.discover();
            String exIp = upnpService.getExternalIPAddress();
            System.out.println("ExternalIP=" + exIp);
            String itIp = upnpService.getInternalHostAddress();
            System.out.println("InternalIP=" + itIp);
            PortMappingInfo addPortMappingInfo = PortMappingInfo.builder()
                    .externalPort(8001)
                    .internalPort(8001)
                    .protocol(ProtocolEnum.TCP)
                    .internalClient("192.168.1.112")
                    .name(UpnpConstant.SOCKET_PORT_MAPPING_NAME)
                    .build();
            boolean resss = upnpService.addPortMapping(addPortMappingInfo);
            System.out.println("result=" + resss);
        } catch (NotDiscoverUpnpGatewayException e) {
            e.printStackTrace();
        } catch (UpnpException e) {
            e.printStackTrace();
            e.printStackTrace();
        }
    }
}
