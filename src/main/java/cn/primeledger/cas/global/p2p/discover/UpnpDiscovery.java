package cn.primeledger.cas.global.p2p.discover;

import cn.primeledger.cas.global.config.Network;
import lombok.extern.slf4j.Slf4j;
import org.bitlet.weupnp.GatewayDevice;
import org.bitlet.weupnp.GatewayDiscover;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.net.InetAddress;
import java.util.Map;

/**
 * The upnp-discovery do mapping for P2P network communication.
 *
 * @author zhao xiaogang
 */

@Slf4j
public class UpnpDiscovery implements Runnable {
    private static final String PROTOCOL = "TCP";

    private Network network;

    public UpnpDiscovery(Network network) {
        this.network = network;
    }

    @Override
    public void run() {
        startUpnp();
    }

    protected void startUpnp() {
        try {
            GatewayDiscover discover = new GatewayDiscover();
            Map<InetAddress, GatewayDevice> devices = discover.discover();

            if (devices.entrySet().size() == 0) {
                LOGGER.info("Not found a upnp gateway device");
                return;
            }

            for (Map.Entry<InetAddress, GatewayDevice> entry : devices.entrySet()) {
                GatewayDevice gw = entry.getValue();
                LOGGER.info("Found a upnp gateway device: local addr = {}, external addr = {}",
                        gw.getLocalAddress().getHostAddress(), gw.getExternalIPAddress());

                int listenPort = network.p2pServerListeningPort();
                gw.deletePortMapping(listenPort, PROTOCOL);
                gw.addPortMapping(listenPort, listenPort, gw.getLocalAddress().getHostAddress(),
                        PROTOCOL, "Add mapping for P2P network");
            }
        } catch (IOException | SAXException | ParserConfigurationException e) {
            LOGGER.info("Failed to add upnp port mapping", e);
        }
    }
}
