package cn.primeledger.cas.global.p2p.store;

import lombok.extern.slf4j.Slf4j;
import org.mapdb.DB;
import org.mapdb.DBMaker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * Peer database for storing peers.
 *
 * @author zhao xiaogang
 */

@Slf4j
public class PeerDatabase {

    private static final String DIRECTOR = "/mapdb/peer";
    private static final String DATA_DIR = "/mapdb/peer/data";
    private static PeerDatabase instance = new PeerDatabase();
    private DB peerDB;
    private PeerMap peerMap;

    public static PeerDatabase getInstance() {
        return instance;
    }

    /**
     * Constructor for the peer database.
     * */
    public PeerDatabase() {
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

        peerMap = new PeerMap(peerDB);
    }

    public PeerMap getPeerMap() {
        return peerMap;
    }

    public void close() {
        if (peerDB != null) {
            peerDB.commit();
            peerDB.close();
            peerDB = null;
        }
    }

}
