package com.higgsblock.global.chain.app.contract;

import com.alibaba.fastjson.annotation.JSONType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Parameters container for contract creation or contract call.
 *
 * @author Chen Jiawei
 * @date 2018-09-27
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@JSONType(includes = {"vmVersion", "bytecode"})
public class ContractParameters {
    /**
     * Version of virtual machine.
     */
    private short vmVersion;
    /**
     * Byte code of contract creation or contract call.
     */
    private byte[] bytecode;

    public ContractParameters(byte[] bytecode) {
        this((short) 0, bytecode);
    }

    public boolean valid() {
        if (vmVersion < 0) {
            return false;
        }

        if (bytecode == null || bytecode.length == 0) {
            return false;
        }

        return true;
    }
}
