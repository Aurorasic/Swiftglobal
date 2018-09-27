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
package com.higgsblock.global.chain.vm.solidity;

import java.util.Arrays;
import java.util.List;

import static com.higgsblock.global.chain.vm.util.ByteUtil.EMPTY_BYTE_ARRAY;

/**
 * Created by Anton Nashatyrev on 26.07.2016.
 */
public abstract class SolidityCallResult  {

    private byte[] executionResult = EMPTY_BYTE_ARRAY;

    public Object getReturnValue() {
        Object[] returnValues = getReturnValues();
        return returnValues.length > 0 ? returnValues[0] : null;
    }

    public Object[] getReturnValues() {

        byte[] executionResult = this.executionResult;
        return getFunction().decodeResult(executionResult);
    }

    public abstract CallTransaction.Function getFunction();

    public void setExecutionResult(byte[] executionResult) {
        this.executionResult = executionResult;
    }
}
