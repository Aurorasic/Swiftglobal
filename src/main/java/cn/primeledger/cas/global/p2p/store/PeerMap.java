package cn.primeledger.cas.global.p2p.store;

import cn.primeledger.cas.global.p2p.Peer;
import org.mapdb.DB;
import org.mapdb.Serializer;

import java.util.Map;

/**
 * Peer map for persistence of peers.
 * @author zhao xiaogang
 */
public class PeerMap extends BaseDatabaseMap<byte[], byte[]> {
    public PeerMap(DB database) {
        super(database);
    }

    @Override
    protected Map<byte[], byte[]> getMap(DB database) {
        return database.treeMap("peers",
                Serializer.BYTE_ARRAY,
                Serializer.BYTE_ARRAY)
                .createOrOpen();
    }

}
