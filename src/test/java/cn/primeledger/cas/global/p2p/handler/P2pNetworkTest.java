package cn.primeledger.cas.global.p2p.handler;

import cn.primeledger.cas.global.config.Network;
import cn.primeledger.cas.global.config.NetworkType;
import cn.primeledger.cas.global.p2p.NetworkMgr;
import org.junit.Test;

public class P2pNetworkTest {

    @Test
    public void testNetwork() {
        startNetwork();
    }

    private void startNetwork() {
        final Network network = new Network.Builder()
                .networkType(NetworkType.DEVNET)
                .build();

//        NetworkMgr networkMgr = new NetworkMgr(network);
//        networkMgr.start();
    }
}
