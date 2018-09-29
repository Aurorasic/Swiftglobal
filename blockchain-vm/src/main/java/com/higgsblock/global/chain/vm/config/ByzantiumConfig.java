package com.higgsblock.global.chain.vm.config;

import com.higgsblock.global.chain.vm.DataWord;
import com.higgsblock.global.chain.vm.GasCost;
import com.higgsblock.global.chain.vm.OpCode;
import com.higgsblock.global.chain.vm.program.Program;
import org.springframework.stereotype.Component;

@Component
public class ByzantiumConfig implements BlockchainConfig {

    private static final GasCost GAS_COST = new GasCost();

    /**
     * block transactions limit exclude coinBase
     */
    private static final int LIMITED_SIZE = 1024 * 1000 * 1;

    /**
     * contract transactions limit exclude coinBase
     */
    private static final int CONTRACT_LIMITED_SIZE = 1024 * 1000 * 1;

    @Override
    public GasCost getGasCost() {
        return GAS_COST;
    }

    @Override
    public Constants getConstants() {
        return new Constants();
    }

    //Since 160HF
    @Override
    public boolean eip161() {
        return true;
    }

    @Override
    public boolean eip198() {
        return true;
    }

    @Override
    public boolean eip206() {
        return true;
    }

    @Override
    public boolean eip211() {
        return true;
    }

    @Override
    public boolean eip212() {
        return true;
    }

    @Override
    public boolean eip213() {
        return true;
    }

    @Override
    public boolean eip214() {
        return true;
    }

    @Override
    public DataWord getCallGas(OpCode op, DataWord requestedGas, DataWord availableGas) throws Program.OutOfGasException {
        if (requestedGas.compareTo(availableGas) > 0) {
            throw Program.Exception.notEnoughOpGas(op, requestedGas, availableGas);
        }
        return requestedGas.clone();
    }

    @Override
    public boolean eip658() {
        return true;
    }

    /**
     * block limit size
     *
     * @return limit size
     */
    @Override
    public int getLimitedSize() {
        return LIMITED_SIZE;
    }

    /**
     * contract limit size
     *
     * @return limit size
     */
    @Override
    public int getContractLimitedSize() {
        return CONTRACT_LIMITED_SIZE;
    }
}
