package com.higgsblock.global.chain.app.config;

import com.higgsblock.global.chain.app.utils.RocksDBWapper;
import lombok.extern.slf4j.Slf4j;
import org.rocksdb.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.UnsupportedEncodingException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
    private AppConfig config;

    /**
     * 区块链数据文件
     */
    private static final String DB_FILE_FLASH = "blockchain.db";
    private static final String DB_FILE_EXTRA= "blockchain-extra.db";
    private static final long DB_FILE_FLASH_SIZE = 10000000000L;
    private static final long DB_FILE_EXTEND_SIZE = 99999999999L;

    private static List<String> columnFamily;

    static {
        columnFamily = new ArrayList<>();
        columnFamily.add("block");
        columnFamily.add("blockIndex");
        columnFamily.add("blockIndexBak");
        columnFamily.add("transactionIndex");
        columnFamily.add("utxo");
        columnFamily.add("score");
        columnFamily.add("witnessBlock");
        columnFamily.add("peer");
        columnFamily.add("witness");
        columnFamily.add("dpos");
        columnFamily.add("dict");
    }

    @Bean
    public RocksDBWapper rocksDBWapper() throws RocksDBException {

        ColumnFamilyOptions options = new ColumnFamilyOptions();

        final List<ColumnFamilyDescriptor> columnFamilyDescriptors =
                new ArrayList<>();

        //must have the default column family
        columnFamilyDescriptors.add(new ColumnFamilyDescriptor(
                RocksDB.DEFAULT_COLUMN_FAMILY, new ColumnFamilyOptions()));

//        List<ColumnFamilyDescriptor> descriptors = columnFamily
//                .stream()
//                .map(item -> new ColumnFamilyDescriptor(item.getBytes("UTF-8"), options))
//                .collect(Collectors.toList());

        List<ColumnFamilyDescriptor> descriptors = new ArrayList<>();
        ColumnFamilyDescriptor descriptor = null;
        for (String item: columnFamily) {
            try {
                descriptor = new ColumnFamilyDescriptor(item.getBytes("UTF-8"), options);
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
            descriptors.add(descriptor);
        }

        columnFamilyDescriptors.addAll(descriptors);

        final List<DbPath> dbPaths = new ArrayList<>();
        final String flashPath = config.getBlockChainDataPath() + "/" + DB_FILE_FLASH;
        final String extraPath = config.getBlockChainDataPath() + "/" + DB_FILE_EXTRA;

        dbPaths.add(new DbPath(Paths.get(flashPath), DB_FILE_FLASH_SIZE));
        dbPaths.add(new DbPath(Paths.get(extraPath), DB_FILE_EXTEND_SIZE));

        final DBOptions dbOptions = new DBOptions()
                .setDbPaths(dbPaths)
                .setCreateIfMissing(true)
                .setCreateMissingColumnFamilies(true);

        List<ColumnFamilyHandle> columnFamilyHandleList = new ArrayList<>();
        RocksDB rocksDB = RocksDB.open(
                dbOptions,
                config.getRootDataPath(),
                columnFamilyDescriptors,
                columnFamilyHandleList);

        Map<String, ColumnFamilyHandle> columnFamilyHandleMap = new HashMap<>(8);
        int size = columnFamily.size();
        for (int i = 0; i < size; i++) {
            columnFamilyHandleMap.put(columnFamily.get(i), columnFamilyHandleList.get(i + 1));
        }

        return new RocksDBWapper(rocksDB, columnFamilyHandleMap);
    }
}