package com.higgsblock.global.chain.network.upnp;

import com.higgsblock.global.chain.crypto.ECKey;
import com.higgsblock.global.chain.network.Peer;
import com.higgsblock.global.chain.network.PeerManager;
import com.higgsblock.global.chain.network.config.PeerConfig;
import com.higgsblock.global.chain.network.enums.NetworkType;
import com.higgsblock.global.chain.network.http.HttpClient;
import com.higgsblock.global.chain.network.upnp.exception.NotDiscoverUpnpGatewayException;
import com.higgsblock.global.chain.network.upnp.exception.UpnpException;
import com.higgsblock.global.chain.network.upnp.model.PortMappingInfo;
import com.higgsblock.global.chain.network.upnp.model.UpnpConstant;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
     * The Peer manager.
     */
    @Autowired
    private PeerManager peerManager;

    /**
     * Init peer info boolean.
     *
     * @return the boolean
     */
    public boolean initPeerInfo() {
        String peerId = ECKey.pubKey2Base58Address(peerConfig.getPubKey());
        Peer dbPeer = peerManager.getById(peerId);
        UpnpDiscover discover = this.getDiscover();
        if (null != dbPeer) {
            if (discover == null) {
                peerManager.setCache(dbPeer);
                return true;
            }

            Peer upnpPeer = queryUpnpPeer(discover);
            if (dbPeer.equals(upnpPeer)) {
                peerManager.setCache(dbPeer);
                return true;
            } else {
                if (!dbPeer.getIp().equals(upnpPeer.getIp())) {
                    dbPeer.setIp(upnpPeer.getIp());
                }

                if (dbPeer.getSocketServerPort() != upnpPeer.getSocketServerPort()) {
                    dbPeer.setSocketServerPort(addMappingSocketPort(discover, dbPeer.getSocketServerPort()));
                }

                if (dbPeer.getHttpServerPort() != upnpPeer.getHttpServerPort()) {
                    dbPeer.setHttpServerPort(addMappingHttpPort(discover, dbPeer.getHttpServerPort()));
                }

                dbPeer.signature(peerConfig.getPriKey());
                peerManager.setCache(dbPeer);
                peerManager.addOrUpdate(dbPeer);
                peerManager.reportToRegistry();
                return true;
            }
        } else {
            Peer peer = new Peer();
            peer.setPubKey(peerConfig.getPubKey());
            peer.setIp(getPublicIp());
            peer.setHttpServerPort(addMappingHttpPort(discover, peerConfig.getHttpPort()));
            peer.setSocketServerPort(addMappingSocketPort(discover, peerConfig.getSocketPort()));
            peer.signature(peerConfig.getPriKey());
            peerManager.setCache(peer);
            peerManager.addOrUpdate(peer);
            peerManager.reportToRegistry();
            return true;
        }
    }

    /**
     * Get socket port int.
     *
     * @return the int
     */
    public int getSocketPort() {
        int port = peerConfig.getSocketPort();
        UpnpDiscover discover = this.getDiscover();
        if (null == discover) {
            return port;
        }

        try {
            String internalIp = discover.getUpnpService().getInternalHostAddress();
            List<PortMappingInfo> allPortMappings = discover.getUpnpService().getAllMappingInfos();
            PortMappingInfo socketPortMapping = allPortMappings.stream()
                    .filter(p -> p.getExternalPort() == peerConfig.getSocketPort()
                            && StringUtils.equals(internalIp, p.getInternalClient())
                            && UpnpConstant.SOCKET_PORT_MAPPING_NAME.equals(p.getName()))
                    .findFirst().orElse(null);
            if (null != socketPortMapping) {
                port = socketPortMapping.getExternalPort();
            } else {
                port = addMappingSocketPort(discover, port);
            }
        } catch (NotDiscoverUpnpGatewayException | UpnpException e) {
            LOGGER.error(e.getMessage(), e);
        }

        return port;
    }

    /**
     * Gets http port.
     *
     * @return the http port
     */
    public int getHttpPort() {
        int port = peerConfig.getHttpPort();
        UpnpDiscover discover = this.getDiscover();
        if (null == discover) {
            return port;
        }

        try {
            String internalIp = discover.getUpnpService().getInternalHostAddress();
            List<PortMappingInfo> allPortMappings = discover.getUpnpService().getAllMappingInfos();
            PortMappingInfo portMappingInfo = allPortMappings.stream()
                    .filter(p -> p.getExternalPort() == peerConfig.getHttpPort()
                            && StringUtils.equals(internalIp, p.getInternalClient())
                            && UpnpConstant.HTTP_PORT_MAPPING_NAME.equals(p.getName()))
                    .findFirst().orElse(null);
            if (null != portMappingInfo) {
                port = portMappingInfo.getExternalPort();
            } else {
                port = addMappingHttpPort(discover, port);
            }
        } catch (NotDiscoverUpnpGatewayException | UpnpException e) {
            LOGGER.error(e.getMessage(), e);
        }

        return port;
    }

    /**
     * Gets public ip.
     *
     * @return the public ip
     */
    public String getPublicIp() {
        if (peerConfig.getNetworkType() == NetworkType.DEV_NET) {
            return peerConfig.getIp();
        }

        String publicIP = null;
        for (String api : UpnpConstant.PUBLIC_ID_APIS) {
            try {
                publicIP = HttpClient.get(api);
            } catch (Exception e) {
                LOGGER.error("get public ip error={},api={}", e.getMessage(), api);
            }

            if (StringUtils.isNotEmpty(publicIP)) {
                break;
            }
        }

        if (StringUtils.isEmpty(publicIP)) {
            try {
                UpnpDiscover discover = this.getDiscover();
                if (null != discover) {
                    publicIP = discover.getUpnpService().getExternalIPAddress();
                }
            } catch (NotDiscoverUpnpGatewayException | UpnpException e) {
                LOGGER.error("try to get public ip by upnp protocol error=" + e.getMessage());
            }
        }

        if (StringUtils.isEmpty(publicIP)) {
            publicIP = peerConfig.getIp();
        }

        LOGGER.info("publicIp={}", publicIP);
        if (isIP(publicIP)) {
            return publicIP;
        }

        return publicIP;
    }

    /**
     * Check the IP format and scope.
     *
     * @param addr: IP address
     */
    private boolean isIP(String addr) {
        if (addr.length() < 7 || addr.length() > 15 || "".equals(addr)) {
            return false;
        }

        String rexp = "^(1\\d{2}|2[0-4]\\d|25[0-5]|[1-9]\\d|[1-9])\\."

                + "(1\\d{2}|2[0-4]\\d|25[0-5]|[1-9]\\d|\\d)\\."

                + "(1\\d{2}|2[0-4]\\d|25[0-5]|[1-9]\\d|\\d)\\."

                + "(1\\d{2}|2[0-4]\\d|25[0-5]|[1-9]\\d|\\d)$";

        Pattern pat = Pattern.compile(rexp);
        Matcher mat = pat.matcher(addr);

        return mat.find();
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
            LOGGER.error(e.getMessage());
            return null;
        }

        return discover;
    }

    /**
     * Build upnp peer peer.
     *
     * @param discover the discover
     * @return the peer
     */
    private Peer queryUpnpPeer(UpnpDiscover discover) {
        Peer peer = new Peer();
        try {
            peer.setPubKey(peerConfig.getPubKey());
            peer.setIp(getPublicIp());

            if (null == discover) {
                return peer;
            }

            String intenalIp = discover.getUpnpService().getInternalHostAddress();

            List<PortMappingInfo> allPortMappings = discover.getUpnpService().getAllMappingInfos();
            PortMappingInfo socketPortMapping = allPortMappings.stream()
                    .filter(p -> p.getExternalPort() == peerConfig.getSocketPort()
                            && StringUtils.equals(intenalIp, p.getInternalClient())
                            && UpnpConstant.SOCKET_PORT_MAPPING_NAME.equals(p.getName())
                            && UpnpConstant.SOCKET_PORT_MAPPING_NAME.equals(p.getName()))
                    .findFirst().orElse(null);
            if (null != socketPortMapping) {
                peer.setSocketServerPort(socketPortMapping.getExternalPort());
            }

            PortMappingInfo httpPortMapping = allPortMappings.stream()
                    .filter(p -> p.getExternalPort() == peerConfig.getHttpPort()
                            && StringUtils.equals(intenalIp, p.getInternalClient())
                            && UpnpConstant.HTTP_PORT_MAPPING_NAME.equals(p.getName())
                            && UpnpConstant.HTTP_PORT_MAPPING_NAME.equals(p.getName()))
                    .findFirst().orElse(null);
            if (null != httpPortMapping) {
                peer.setHttpServerPort(httpPortMapping.getExternalPort());
            }

        } catch (UpnpException | NotDiscoverUpnpGatewayException e) {
            peer.setHttpServerPort(-1);
            peer.setSocketServerPort(-1);
            LOGGER.error("query port mapping info error:" + e.getMessage(), e);
        } finally {
            peer.signature(peerConfig.getPriKey());
        }

        return peer;
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
            return peerConfig.getSocketPort();
        }

        int port = initPort;
        try {
            PortMappingInfo mappingInfo = discover.autoMapPort(initPort, peerConfig.getSocketPort(), UpnpConstant.SOCKET_PORT_MAPPING_NAME);
            if (null != mappingInfo) {
                port = mappingInfo.getExternalPort();
                LOGGER.info("upnp socket port mapping info successful：" + mappingInfo);
            }
        } catch (NotDiscoverUpnpGatewayException | UpnpException e) {
            port = peerConfig.getSocketPort();
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
            return peerConfig.getHttpPort();
        }

        int port = initPort;
        try {
            PortMappingInfo mappingInfo = discover.autoMapPort(initPort, peerConfig.getHttpPort(), UpnpConstant.HTTP_PORT_MAPPING_NAME);
            if (null != mappingInfo) {
                port = mappingInfo.getExternalPort();
                LOGGER.info("upnp http port mapping info successful：" + mappingInfo);
            }
        } catch (NotDiscoverUpnpGatewayException | UpnpException e) {
            port = peerConfig.getHttpPort();
            LOGGER.error(e.getMessage(), e);
        }

        return port;
    }
}