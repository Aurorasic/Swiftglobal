package com.higgsblock.global.chain.vm.api;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author Chen Jiawei
 * @date 2018-09-17
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ExecutionEnvironment {
    private boolean isCreate;
    private byte[] receiveAddress;
    private byte[] sendAddress;
    private byte[] gasPrice;
    private byte[] gasLimit;
    private byte[] value;
    private byte[] data;
}
