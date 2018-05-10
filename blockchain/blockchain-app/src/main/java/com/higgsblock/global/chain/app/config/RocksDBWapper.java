package com.higgsblock.global.chain.app.config;


import lombok.AllArgsConstructor;
import lombok.Data;
import org.rocksdb.ColumnFamilyHandle;
import org.rocksdb.RocksDB;

import java.util.Map;

@Data
@AllArgsConstructor
public class RocksDBWapper {
    private RocksDB rocksDB;
    private Map<String, ColumnFamilyHandle> columnFamilyHandleMap;
}
