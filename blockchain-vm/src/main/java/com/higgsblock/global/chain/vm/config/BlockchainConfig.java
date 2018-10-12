package com.higgsblock.global.chain.vm.config;

import com.higgsblock.global.chain.vm.DataWord;
import com.higgsblock.global.chain.vm.GasCost;
import com.higgsblock.global.chain.vm.OpCode;
import com.higgsblock.global.chain.vm.program.Program;

public interface BlockchainConfig {
    /**
     * EVM operations costs
     */
    GasCost getGasCost();

    /**
     * Get blockchain constants
     */
    Constants getConstants();

    /**
     * EIP161: https://github.com/ethereum/EIPs/issues/161
     */
    boolean eip161();

    /**
     * EIP198: https://github.com/ethereum/EIPs/pull/198
     */
    boolean eip198();

    /**
     * EIP206: https://github.com/ethereum/EIPs/pull/206
     */
    boolean eip206();

    /**
     * EIP211: https://github.com/ethereum/EIPs/pull/211
     */
    boolean eip211();

    /**
     * EIP212: https://github.com/ethereum/EIPs/pull/212
     */
    boolean eip212();

    /**
     * EIP213: https://github.com/ethereum/EIPs/pull/213
     */
    boolean eip213();

    /**
     * EIP214: https://github.com/ethereum/EIPs/pull/214
     */
    boolean eip214();

    /**
     * EIP658: https://github.com/ethereum/EIPs/pull/658
     * Replaces the intermediate state root field of the receipt with the status
     */
    boolean eip658();

    /**
     * Calculates available gas to be passed for callee
     * Since EIP150
     * @param op  Opcode
     * @param requestedGas amount of gas requested by the program
     * @param availableGas available gas
     * @throws Program.OutOfGasException If passed args doesn't conform to limitations
     */
    DataWord getCallGas(OpCode op, DataWord requestedGas, DataWord availableGas) throws Program.OutOfGasException;

    /**
     * block limit size
     * @return limit size
     */
    int getLimitedSize();

    /**
     * contract limit size
     * @return limit size
     */
    int getContractLimitedSize();

    /**
     * Gets limited used gas in a block.
     *
     * @return amount of limited used gas.
     */
    long getBlockGasLimit();
}
