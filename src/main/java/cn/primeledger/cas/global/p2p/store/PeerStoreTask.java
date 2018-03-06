package cn.primeledger.cas.global.p2p.store;

import cn.primeledger.cas.global.p2p.Peer;
import cn.primeledger.cas.global.p2p.PeerMgr;

import java.util.Deque;

/**
 * The task stores peers to database.
 *
 * @author zhao xiaogang
 * */
public class PeerStoreTask implements Runnable{
    private PeerMgr peerMgr;

    public PeerStoreTask(PeerMgr peerMgr) {
        this.peerMgr = peerMgr;
    }


    @Override
    public void run() {
        save();
    }

    private void save() {
        Deque<Peer> peers = peerMgr.getPeers();
        PeerDatabase db = PeerDatabase.getInstance();
        byte[] addressBytes;

        for (Peer peer :peers) {
            addressBytes = peer.getAddress().getAddress().getAddress();
            db.getPeerMap().map.put(addressBytes, peer.toBytes());
        }
        db.close();
    }
}
