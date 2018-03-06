package cn.primeledger.cas.global.p2p.store;

import cn.primeledger.cas.global.p2p.RegisterCenter;
import cn.primeledger.cas.global.p2p.RegistryPeer;

import java.util.List;

/**
 * @author yuanjiantao
 * @date 3/6/2018
 */
public class RegistryPeerStoreTask implements Runnable {
    private RegisterCenter registerCenter;

    public RegistryPeerStoreTask(RegisterCenter registerCenter) {
        this.registerCenter = registerCenter;
    }


    @Override
    public void run() {
        save();
    }

    private void save() {
        List<RegistryPeer> list = registerCenter.getRegistryPeers();
        RegistryPeerDatabase db = RegistryPeerDatabase.getInstance();
        byte[] addressBytes;
        list.forEach(registryPeer -> {
            String host = registryPeer.getIp() + ":" + registryPeer.getPort();
            db.getRegistryPeerMap().map.put(registryPeer.getPubKey().getBytes(), host.getBytes());
        });

        db.close();
    }
}
