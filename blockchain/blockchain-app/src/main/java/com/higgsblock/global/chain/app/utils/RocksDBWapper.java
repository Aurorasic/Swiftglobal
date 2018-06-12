package com.higgsblock.global.chain.app.utils;


import lombok.AllArgsConstructor;
import lombok.Data;
import org.rocksdb.ColumnFamilyHandle;
import org.rocksdb.RocksDB;

import java.util.Map;


/**
 * @author zhao xiaogang
 * @create 2018-05-21
 */

@Data
@AllArgsConstructor
public class RocksDBWapper {
    private RocksDB rocksDB;
    private Map<String, ColumnFamilyHandle> columnFamilyHandleMap;
}
