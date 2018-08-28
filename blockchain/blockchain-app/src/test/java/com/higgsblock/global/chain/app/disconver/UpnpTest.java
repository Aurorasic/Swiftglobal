package com.higgsblock.global.chain.app.disconver;

import com.higgsblock.global.chain.app.BaseTest;
import com.higgsblock.global.chain.app.net.api.IRegistryApi;
import com.higgsblock.global.chain.app.net.peer.PeerManager;
import com.higgsblock.global.chain.network.upnp.UpnpDiscover;
import com.higgsblock.global.chain.network.upnp.UpnpManager;
import com.higgsblock.global.chain.network.upnp.model.PortMappingInfo;
import com.higgsblock.global.chain.network.upnp.model.UpnpConstant;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * The type Upnp test.
 *
 * @author yanghuadong
 * @date 2018 -05-21
 */
public class UpnpTest extends BaseTest {
    /**
     * The Upnp manager.
     */
    @Autowired
    private UpnpManager upnpManager;

    /**
     * The Upnp discover.
     */
    private UpnpDiscover upnpDiscover = new UpnpDiscover();

    /**
     * The Registry api.
     */
    @Autowired
    private IRegistryApi registryApi;

    @Autowired
    private PeerManager peerManager;

    /**
     * Init port test.
     *
     * @throws Exception the exception
     */
    @Test
    public void initPortTest() throws Exception {
        peerManager.reportAndGetPeers();

        boolean state = this.peerManager.loadSelfPeerInfo();
        System.out.println("init port result = " + state);
    }

    /**
     * Socket port test.
     */
    @Test
    public void socketPortTest() {
        int port = upnpManager.getHttpMappingPort();
        System.out.println("port = " + port);
    }

    /**
     * Add port mapping test.
     *
     * @throws Exception the exception
     */
    @Test
    public void addPortMappingTest() throws Exception {
        PortMappingInfo mappingInfo = upnpDiscover.autoMapPort(8001, 8001, UpnpConstant.SOCKET_PORT_MAPPING_NAME);
        System.out.println(mappingInfo);
        Assert.assertNotEquals(null, mappingInfo);
    }
}