package com.higgsblock.global.chain.vm.config;

import com.higgsblock.global.chain.vm.DataWord;
import com.higgsblock.global.chain.vm.GasCost;
import com.higgsblock.global.chain.vm.OpCode;
import com.higgsblock.global.chain.vm.program.Program;

public class ByzantiumConfig implements BlockchainConfig {

    private static final GasCost GAS_COST = new GasCost();

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
}
