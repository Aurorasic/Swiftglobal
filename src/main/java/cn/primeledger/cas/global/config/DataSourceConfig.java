package cn.primeledger.cas.global.config;

import cn.primeledger.cas.global.bean.HTreeMapDelegate;
import cn.primeledger.cas.global.blockchain.Block;
import cn.primeledger.cas.global.blockchain.BlockIndex;
import cn.primeledger.cas.global.blockchain.transaction.TransactionIndex;
import cn.primeledger.cas.global.blockchain.transaction.UTXO;
import cn.primeledger.cas.global.crypto.ECKey;
import cn.primeledger.cas.global.crypto.model.KeyPair;
import org.mapdb.DB;
import org.mapdb.DBMaker;
import org.mapdb.HTreeMap;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.concurrent.ConcurrentMap;

/**
 * data source config
 *
 * @author baizhengwen
 * @date 2018/2/24
 */
@Configuration
public class DataSourceConfig {

    @Autowired
    private KeyPair peerKeyPair;

    @Bean
    public DB memoryDB() {
        return DBMaker.memoryDB().make();
    }

    @Bean
    public DB blockChainDB(AppConfig config) throws IOException {
        Files.createDirectories(Paths.get(config.getBlockChainDataPath()));
        return DBMaker.fileDB(config.getBlockChainDataFile())
                .transactionEnable()
                .closeOnJvmShutdown()
                .make();
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
}
