package com.higgsblock.global.chain.app.config;

import com.higgsblock.global.chain.app.Application;
import com.higgsblock.global.chain.app.bean.HTreeMapDelegate;
import com.higgsblock.global.chain.app.blockchain.Block;
import com.higgsblock.global.chain.app.blockchain.BlockIndex;
import com.higgsblock.global.chain.app.blockchain.transaction.TransactionIndex;
import com.higgsblock.global.chain.app.blockchain.transaction.UTXO;
import com.higgsblock.global.chain.app.consensus.ScoreManager;
import com.higgsblock.global.chain.network.Peer;
import com.higgsblock.global.chain.crypto.ECKey;
import com.higgsblock.global.chain.crypto.KeyPair;
import lombok.extern.slf4j.Slf4j;
import org.mapdb.*;
import org.rocksdb.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.ConcurrentMap;
import java.util.stream.Collectors;

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

    /**
     * 区块链数据文件
     */
    private static final String DB_FILE = "blockchain.db";
    private static final String DB_FILE1 = "blockchain1.db";

    private List<String> columnFamily = Arrays.asList("peer", "block");

    @Bean
    public RocksDBWapper rocksDBWapper(AppConfig config) throws RocksDBException {

        ColumnFamilyOptions options = new ColumnFamilyOptions();

        final List<ColumnFamilyDescriptor> columnFamilyDescriptors =
                new ArrayList<>();

        columnFamilyDescriptors.add(new ColumnFamilyDescriptor(
                RocksDB.DEFAULT_COLUMN_FAMILY, new ColumnFamilyOptions()));

        List<ColumnFamilyDescriptor> descriptors = columnFamily
                .stream()
                .map(item -> new ColumnFamilyDescriptor(item.getBytes(), options))
                .collect(Collectors.toList());

        columnFamilyDescriptors.addAll(descriptors);

        final List<DbPath> dbPaths = new ArrayList<>();
        dbPaths.add(new DbPath(Paths.get(DB_FILE), 10));
        dbPaths.add(new DbPath(Paths.get(DB_FILE1), 100));

        final DBOptions dbOptions = new DBOptions()
                //.setDbPaths(dbPaths)
                .setCreateIfMissing(true)
                .setCreateMissingColumnFamilies(true);

        List<ColumnFamilyHandle> columnFamilyHandleList = new ArrayList<>();
        RocksDB rocksDB = RocksDB.open(dbOptions,
                config.getRootDataPath(), columnFamilyDescriptors,
                columnFamilyHandleList);

        Map<String, ColumnFamilyHandle> columnFamilyHandleMap = new HashMap<>();
        int size = columnFamily.size();
        for (int i = 0; i < size; i++) {
            columnFamilyHandleMap.put(columnFamily.get(i), columnFamilyHandleList.get(i + 1));
        }

        return new RocksDBWapper(rocksDB, columnFamilyHandleMap);
    }


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
        HTreeMap<String, Block> map = (HTreeMap<String, Block>) blockChainDB
                .hashMap("block")
                .createOrOpen();
        return new HTreeMapDelegate<>(blockChainDB, map);
    }

    @Bean
    public ConcurrentMap<Long, BlockIndex> blockIndexData(DB blockChainDB) {
        //block index table data
        HTreeMap<Long, BlockIndex> map = (HTreeMap<Long, BlockIndex>) blockChainDB
                .hashMap("blockIndex")
                .createOrOpen();
        return new HTreeMapDelegate<>(blockChainDB, map);
    }

    @Bean
    public ConcurrentMap<String, TransactionIndex> transactionIndexData(DB blockChainDB) {
        //transaction index table data
        HTreeMap<String, TransactionIndex> map = (HTreeMap<String, TransactionIndex>) blockChainDB
                .hashMap("transactionIndex")
                .createOrOpen();
        return new HTreeMapDelegate<>(blockChainDB, map);
    }

    @Bean
    public ConcurrentMap<String, UTXO> utxoData(DB blockChainDB) {
        //transaction utxo table data
        HTreeMap<String, UTXO> map = (HTreeMap<String, UTXO>) blockChainDB
                .hashMap("utxo")
                .createOrOpen();
        return new HTreeMapDelegate<>(blockChainDB, map);
    }

    @Bean
    public ConcurrentMap<String, UTXO> myUTXOData(DB blockChainDB) {
        //transaction utxo table data
        String address = ECKey.pubKey2Base58Address(peerKeyPair.getPubKey());
        HTreeMap<String, UTXO> map = (HTreeMap<String, UTXO>) blockChainDB
                .hashMap(address)
                .createOrOpen();
        return new HTreeMapDelegate<>(blockChainDB, map);
    }

    @Bean
    public ConcurrentMap<String, Map> minerScoreMaps(DB blockChainDB) {
        //miner score infos
        HTreeMap<String, Map> map = (HTreeMap<String, Map>) blockChainDB
                .hashMap("minerScoreMaps")
                .createOrOpen();
        if (map.get(ScoreManager.ALL_SCORE_KEY) == null) {
            map.put("allMinerSoreMap", new HashMap<String, Integer>(1000));
        }
        if (map.get(ScoreManager.TMP_SCORE_KEY) == null) {
            map.put("tmpMinerSoreMap", new HashMap<String, Integer>(1000));
        }
        if (map.get(ScoreManager.DPOS_SCORE_KEY) == null) {
            map.put("dposMinerSoreMap", new HashMap<String, Integer>(1000));
        }
        return new HTreeMapDelegate<>(blockChainDB, map);
    }

    @Bean
    public ConcurrentMap<Long, Block> witnessedBlock(DB blockChainDB) {
        //block index table data
        HTreeMap<Long, Block> map = (HTreeMap<Long, Block>) blockChainDB
                .hashMap("witnessedBlock")
                .createOrOpen();
        return new HTreeMapDelegate<>(blockChainDB, map);
    }

    @Bean
    public ConcurrentMap<String, Peer> peerMap(DB blockChainDB) {
        //block index table data
        HTreeMap<String, Peer> map = (HTreeMap<String, Peer>) blockChainDB
                .hashMap("peer")
                .createOrOpen();
        return new HTreeMapDelegate<>(blockChainDB, map);
    }

    @Bean
    public BTreeMap<byte[], byte[]> pubKeyMap(DB blockChainDB) {
        //block index table data
        BTreeMap<byte[], byte[]> map = blockChainDB
                .treeMap("pubKeyMap")
                .keySerializer(Serializer.BYTE_ARRAY)
                .valueSerializer(Serializer.BYTE_ARRAY)
                .createOrOpen();
//        return new BTreeMapDelegate<>(blockChainDB, map);
        return map;
    }
}