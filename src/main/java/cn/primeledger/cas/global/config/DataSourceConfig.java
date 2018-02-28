package cn.primeledger.cas.global.config;

import cn.primeledger.cas.global.blockchain.Block;
import cn.primeledger.cas.global.blockchain.BlockIndex;
import cn.primeledger.cas.global.blockchain.transaction.TransactionIndex;
import org.mapdb.DB;
import org.mapdb.DBMaker;
import org.mapdb.HTreeMap;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * data source config
 *
 * @author baizhengwen
 * @date Created in 2018/2/24
 */
@Configuration
public class DataSourceConfig {

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
    public HTreeMap<String, Block> blockData(DB blockChainDB) {
        //block table data
        return (HTreeMap<String, Block>) blockChainDB.hashMap("block").createOrOpen();
    }

    @Bean
    public HTreeMap<String, BlockIndex> blockIndexData(DB blockChainDB) {
        //block index table data
        return (HTreeMap<String, BlockIndex>) blockChainDB.hashMap("blockIndex").createOrOpen();
    }

    @Bean
    public HTreeMap<String, TransactionIndex> transactionIndexData(DB blockChainDB) {
        //transaction index table data
        return (HTreeMap<String, TransactionIndex>) blockChainDB.hashMap("transactionIndex").createOrOpen();
    }

}
