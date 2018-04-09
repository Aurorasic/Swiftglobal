package cn.primeledger.cas.global.schedule;

import cn.primeledger.cas.global.config.AppConfig;
import cn.primeledger.cas.global.network.Peer;
import cn.primeledger.cas.global.network.PeerManager;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.concurrent.TimeUnit;

/**
 * @author baizhengwen
 * @date 2018/3/22
 */
//@Component
public class InetAddressCheckTask extends BaseTask {

    @Autowired
    private AppConfig config;

    @Autowired
    private PeerManager peerManager;

    @Override
    protected void task() {
        // todo baizhengwen check public ip address
        // todo baizhengwen check public port

        Peer peer = new Peer();
        peer.setIp(config.getClientPublicIp());
        // todo baizhengwen 通过upnp设置端口
        peer.setSocketServerPort(config.getSocketServerPort());
        peer.setHttpServerPort(config.getHttpServerPort());
        peer.setPubKey(config.getPubKey());
        peer.signature(config.getPriKey());

//        peerManager.setSelf(peer);
    }

    @Override
    protected long getPeriodMs() {
        return TimeUnit.HOURS.toMillis(1);
    }
}
