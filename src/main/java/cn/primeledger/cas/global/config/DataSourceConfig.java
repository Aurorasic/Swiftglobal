package cn.primeledger.cas.global.config;

import cn.primeledger.cas.global.Application;
import cn.primeledger.cas.global.bean.HTreeMapDelegate;
import cn.primeledger.cas.global.blockchain.Block;
import cn.primeledger.cas.global.blockchain.BlockIndex;
import cn.primeledger.cas.global.blockchain.transaction.TransactionIndex;
import cn.primeledger.cas.global.blockchain.transaction.UTXO;
import cn.primeledger.cas.global.crypto.ECKey;
import cn.primeledger.cas.global.crypto.model.KeyPair;
import cn.primeledger.cas.global.network.Peer;
import lombok.extern.slf4j.Slf4j;
import org.mapdb.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.ConcurrentMap;

/**
 * data source config
 *
 * @author baizhengwen
 * @date 2018/2/24
 */
@Configuration
@Slf4j
public class DataSourceConfig {

    @Autowired
    private KeyPair peerKeyPair;

    @Bean
    public DB memoryDB() {
        return DBMaker.memoryDB().make();
    }

    @Bean
    public DB blockChainDB(AppConfig config) throws IOException {
        if (Application.TEST) {
            testClearDBData(config);
        }
        Files.createDirectories(Paths.get(config.getBlockChainDataPath()));
        return DBMaker.fileDB(config.getBlockChainDataFile())
                .transactionEnable()
                .closeOnJvmShutdown()
                .make();
    }

    private void testClearDBData(AppConfig config) {
        //delete db file
        Path path = Paths.get(config.getBlockChainDataFile());
        File file = new File(path.toFile().toURI());
        if (file.exists()) {
            boolean delete = file.delete();
            if (delete) {
                LOGGER.info("deleted file: {}", path.toAbsolutePath());
            } else {
                throw new RuntimeException("can not deleted file: " + path.toAbsolutePath());
            }
        } else {
            LOGGER.info("file({}) does not exit", path.toAbsolutePath());
        }

        //delete log file
    }

    @Bean
    public ConcurrentMap<String, Block> blockData(DB blockChainDB) {
        //block table data
        HTreeMap<String, Block> map = (HTreeMap<String, Block>) blockChainDB.hashMap("block").createOrOpen();
        return new HTreeMapDelegate<>(blockChainDB, map);
    }

    @Bean
    public ConcurrentMap<Long, BlockIndex> blockIndexData(DB blockChainDB) {
        //block index table data
        HTreeMap<Long, BlockIndex> map = (HTreeMap<Long, BlockIndex>) blockChainDB.hashMap("blockIndex").createOrOpen();
        return new HTreeMapDelegate<>(blockChainDB, map);
    }

    @Bean
    public ConcurrentMap<String, TransactionIndex> transactionIndexData(DB blockChainDB) {
        //transaction index table data
        HTreeMap<String, TransactionIndex> map = (HTreeMap<String, TransactionIndex>) blockChainDB.hashMap("transactionIndex").createOrOpen();
        return new HTreeMapDelegate<>(blockChainDB, map);
    }

    @Bean
    public ConcurrentMap<String, UTXO> utxoData(DB blockChainDB) {
        //transaction utxo table data
        HTreeMap<String, UTXO> map = (HTreeMap<String, UTXO>) blockChainDB.hashMap("utxo").createOrOpen();
        return new HTreeMapDelegate<>(blockChainDB, map);
    }

    @Bean
    public ConcurrentMap<String, UTXO> myUTXOData(DB blockChainDB) {
        //transaction utxo table data
        String address = ECKey.pubKey2Base58Address(peerKeyPair.getPubKey());
        HTreeMap<String, UTXO> map = (HTreeMap<String, UTXO>) blockChainDB.hashMap(address).createOrOpen();
        return new HTreeMapDelegate<>(blockChainDB, map);
    }

    @Bean
    public ConcurrentMap<Long, Block> witnessedBlock(DB blockChainDB) {
        //block index table data
        HTreeMap<Long, Block> map = (HTreeMap<Long, Block>) blockChainDB.hashMap("witnessedBlock").createOrOpen();
        return new HTreeMapDelegate<>(blockChainDB, map);
    }

    @Bean
    public ConcurrentMap<String, Peer> peerMap(DB blockChainDB) {
        //block index table data
        HTreeMap<String, Peer> map = (HTreeMap<String, Peer>) blockChainDB.hashMap("peer").createOrOpen();
        return new HTreeMapDelegate<>(blockChainDB, map);
    }

    @Bean
    public BTreeMap<byte[], byte[]> pubKeyMap(DB blockChainDB) {
        //block index table data
        BTreeMap<byte[], byte[]> map = blockChainDB.treeMap("pubKeyMap")
                .keySerializer(Serializer.BYTE_ARRAY)
                .valueSerializer(Serializer.BYTE_ARRAY)
                .createOrOpen();
//        return new BTreeMapDelegate<>(blockChainDB, map);
        return map;
    }
}
