package cn.primeledger.cas.global.p2p.store;

import lombok.extern.slf4j.Slf4j;
import org.mapdb.DB;
import org.mapdb.DBMaker;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * Peer database for storing peers.
 *
 * @author zhao xiaogang
 */

@Slf4j
public class RegistryPeerDatabase {

    private static final String DIRECTOR = "/mapdb/registrypeer";
    private static final String DATA_DIR = "/mapdb/registrypeer/data";
    private static RegistryPeerDatabase instance = new RegistryPeerDatabase();
    private DB peerDB;
    private RegistryPeerMap registryPeerMap;

    /**
     * Constructor for the peer database.
     */
    public RegistryPeerDatabase() {
        try {
            Files.createDirectories(Paths.get(DIRECTOR));
        } catch (IOException e) {
            if (e != null) {
                LOGGER.error("Create db error: {}", e.getMessage());
            }
        }

        peerDB = DBMaker.fileDB(DATA_DIR)
                .transactionEnable()
                .closeOnJvmShutdown()
                .make();

        registryPeerMap = new RegistryPeerMap(peerDB);
    }

    public static RegistryPeerDatabase getInstance() {
        return instance;
    }

    public RegistryPeerMap getRegistryPeerMap() {
        return registryPeerMap;
    }

    public void close() {
        if (peerDB != null) {
            peerDB.commit();
            peerDB.close();
            peerDB = null;
        }
    }

}
