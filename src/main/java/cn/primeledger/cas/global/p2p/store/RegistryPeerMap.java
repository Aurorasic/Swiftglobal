package cn.primeledger.cas.global.p2p.store;

import org.mapdb.DB;
import org.mapdb.Serializer;

import java.util.Map;

/**
 * Peer map for persistence of peers.
 *
 * @author zhao xiaogang
 */
public class RegistryPeerMap extends BaseDatabaseMap<byte[], byte[]> {
    public RegistryPeerMap(DB database) {
        super(database);
    }

    @Override
    protected Map<byte[], byte[]> getMap(DB database) {
        return database.treeMap("registryPeers",
                Serializer.BYTE_ARRAY,
                Serializer.BYTE_ARRAY)
                .createOrOpen();
    }

}
