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
package com.higgsblock.global.chain.vm.util;

import com.higgsblock.global.chain.vm.core.AccountState;

import org.springframework.util.SerializationUtils;

/**
 * Collection of common Serializers
 * Created by Anton Nashatyrev on 08.11.2016.
 */
public class Serializers {

    /**
     *  No conversion
     */
    public static class Identity<T> implements Serializer<T, T> {
        @Override
        public T serialize(T object) {
            return object;
        }
        @Override
        public T deserialize(T stream) {
            return stream;
        }
    }


    /**
     * Serializes/Deserializes AccountState instances from the State Trie (part of Ethereum spec)
     */
    public final static Serializer<AccountState, byte[]> AccountStateSerializer = new Serializer<AccountState, byte[]>() {
        @Override
        public byte[] serialize(AccountState object) {
            return SerializationUtils.serialize(object);
        }

        @Override
        public AccountState deserialize(byte[] stream) {
            return stream == null || stream.length == 0 ? null : (AccountState)SerializationUtils.deserialize(stream);
        }
    };


    /**
     * AS IS serializer (doesn't change anything)
     */
    public final static Serializer<byte[], byte[]> AsIsSerializer = new Serializer<byte[], byte[]>() {
        @Override
        public byte[] serialize(byte[] object) {
            return object;
        }

        @Override
        public byte[] deserialize(byte[] stream) {
            return stream;
        }
    };
}
