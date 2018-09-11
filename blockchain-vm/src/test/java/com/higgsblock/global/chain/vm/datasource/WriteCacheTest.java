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


import com.higgsblock.global.chain.vm.DataWord;
import org.junit.Test;
import org.spongycastle.util.encoders.Hex;


import static com.higgsblock.global.chain.vm.util.ByteUtil.longToBytes;
import static com.higgsblock.global.chain.vm.util.HashUtil.sha3;
import static org.junit.Assert.*;

/**
 * Testing {@link WriteCache}
 */
public class WriteCacheTest {

    private byte[] intToKey(int i) {
        return sha3(longToBytes(i));
    }

    private byte[] intToValue(int i) {
        return (new DataWord(i)).getData();
    }

    private String str(Object obj) {
        if (obj == null) return null;
        return Hex.toHexString((byte[]) obj);
    }

    @Test
    public void testSimple() {
        Source<byte[], byte[]> src = new HashMapDB<>();
        WriteCache<byte[], byte[]> writeCache = new WriteCache.BytesKey<>(src, WriteCache.CacheType.SIMPLE);
        for (int i = 0; i < 10_000; ++i) {
            writeCache.put(intToKey(i), intToValue(i));
        }
        // Everything is cached
        assertEquals(str(intToValue(0)), str(writeCache.getCached(intToKey(0)).value()));
        assertEquals(str(intToValue(9_999)), str(writeCache.getCached(intToKey(9_999)).value()));

        // Everything is flushed
        writeCache.flush();
        assertNull(writeCache.getCached(intToKey(0)));
        assertNull(writeCache.getCached(intToKey(9_999)));
        assertEquals(str(intToValue(9_999)), str(writeCache.get(intToKey(9_999))));
        assertEquals(str(intToValue(0)), str(writeCache.get(intToKey(0))));
        // Get not caches, only write cache
        assertNull(writeCache.getCached(intToKey(0)));

        // Deleting key that is currently in cache
        writeCache.put(intToKey(0), intToValue(12345));
        assertEquals(str(intToValue(12345)), str(writeCache.getCached(intToKey(0)).value()));
        writeCache.delete(intToKey(0));
        assertTrue(null == writeCache.getCached(intToKey(0)) || null == writeCache.getCached(intToKey(0)).value());
        assertEquals(str(intToValue(0)), str(src.get(intToKey(0))));
        writeCache.flush();
        assertNull(src.get(intToKey(0)));

        // Deleting key that is not currently in cache
        assertTrue(null == writeCache.getCached(intToKey(1)) || null == writeCache.getCached(intToKey(1)).value());
        assertEquals(str(intToValue(1)), str(src.get(intToKey(1))));
        writeCache.delete(intToKey(1));
        assertTrue(null == writeCache.getCached(intToKey(1)) || null == writeCache.getCached(intToKey(1)).value());
        assertEquals(str(intToValue(1)), str(src.get(intToKey(1))));
        writeCache.flush();
        assertNull(src.get(intToKey(1)));
    }

}
