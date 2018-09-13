package com.higgsblock.global.chain.app.keyvalue.db;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * @author baizhengwen
 * @date 2018-09-12
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DataItem {
    private Serializable keyspace;
    private Serializable key;
    private Object value;
}
