/*
 * Copyright (c) [2016] [ <ether.camp> ]
 * This file is part of the ethereumJ library.
 *
 * The ethereumJ library is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * The ethereumJ library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with the ethereumJ library. If not, see <http://www.gnu.org/licenses/>.
 */
package com.higgsblock.global.chain.vm.datasource;

import com.google.common.base.Charsets;
import com.google.common.hash.HashFunction;
import com.google.common.hash.Hashing;
import lombok.extern.slf4j.Slf4j;
import org.spongycastle.util.encoders.Hex;
import org.springframework.util.SerializationUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

/**
 * Clue class between Source and BatchSource
 * <p>
 * Created by Anton Nashatyrev on 29.11.2016.
 */
@Slf4j
public class BatchSourceWriter<Key, Value> extends AbstractChainedSource<Key, Value, Key, Value> {

    Map<Key, Value> buf = new HashMap<>();

    public BatchSourceWriter(BatchSource<Key, Value> src) {
        super(src);
    }

    private BatchSource<Key, Value> getBatchSource() {
        return (BatchSource<Key, Value>) getSource();
    }

    @Override
    public synchronized void delete(Key key) {
        buf.put(key, null);
    }

    @Override
    public synchronized void put(Key key, Value val) {
        buf.put(key, val);
    }

    @Override
    public Value get(Key key) {
        LOGGER.info("get key:{}", Hex.toHexString((byte[]) key));
        return getSource().get(key);
    }

    @Override
    public synchronized boolean flushImpl() {
        if (!buf.isEmpty()) {
            getBatchSource().updateBatch(buf);
            buf.clear();
            return true;
        } else {
            return false;
        }
    }

    /**
     * return cache hash
     * hash=hash(key1=value1&key2=value2)
     *
     * @return
     */
    @Override
    public String getStateHash() {
        StringBuilder sb = new StringBuilder();
        LOGGER.info("value size" + buf.entrySet().size());

        Map<String, byte[]> keyMap = new TreeMap<>();
        for (Key key : buf.keySet()) {
            keyMap.put(Hex.toHexString((byte[]) key), (byte[]) key);
        }

        for (String key : keyMap.keySet()) {
            LOGGER.info("bufKey:{}， bufValue:{}", key, Hex.toHexString((byte[]) buf.get(keyMap.get(key))));
            sb.append(key).append("=").append(Hex.toHexString((byte[]) buf.get(keyMap.get(key)))).append("&");

            if (Hex.toHexString((byte[]) buf.get(keyMap.get(key))).startsWith("aced")) {
                LOGGER.info("####value:{}", SerializationUtils.deserialize((byte[]) buf.get(keyMap.get(key))));
            }
        }

        HashFunction function = Hashing.sha256();
        LOGGER.info("hash:{}", sb.toString());
        return function.hashString(sb.toString(), Charsets.UTF_8).toString();
    }

}
