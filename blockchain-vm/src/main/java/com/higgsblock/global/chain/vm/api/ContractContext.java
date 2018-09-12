package com.higgsblock.global.chain.vm.api;

import com.higgsblock.global.chain.vm.DataWord;
import com.higgsblock.global.chain.vm.GasCost;
import com.higgsblock.global.chain.vm.OpCode;
import com.higgsblock.global.chain.vm.VM;
import com.higgsblock.global.chain.vm.config.BlockchainConfig;
import com.higgsblock.global.chain.vm.config.Constants;
import com.higgsblock.global.chain.vm.core.SystemProperties;
import com.higgsblock.global.chain.vm.program.Program;

/**
 * @author Chen Jiawei
 * @date 2018-09-12
 */
public class ContractContext {
    private void run() {
        BlockchainConfig blockchainConfig =new BlockchainConfig(){
            @Override
            public Constants getConstants() {
                return new Constants();
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
                return false;
            }

            @Override
            public boolean eip213() {
                return false;
            }

            @Override
            public boolean eip214() {
                return true;
            }

            @Override
            public boolean eip658() {
                return false;
            }

            @Override
            public GasCost getGasCost() {
                return new GasCost();
            }

            @Override
            public boolean eip161() {
                return true;
            }

            @Override
            public boolean eip198() {
                return false;
            }

            @Override
            public DataWord getCallGas(OpCode op, DataWord requestedGas, DataWord availableGas) throws Program.OutOfGasException {
                if (requestedGas.compareTo(availableGas) > 0) {
                    throw Program.Exception.notEnoughOpGas(op, requestedGas, availableGas);
                }
                return requestedGas.clone();
            }
        };

        Program program = new Program(null, null, null, null) {
            @Override
            public void saveOpTrace() {
            }

            public BlockchainConfig getBlockchainConfig() {
                return blockchainConfig;
            }
        };

        SystemProperties systemProperties = new SystemProperties() {
            @Override
            public boolean vmTrace() {
                return false;
            }
        };

        VM vm = new VM(systemProperties);
        vm.play(program);

    }

    public static void main(String[] args) {
        ContractContext contractContext = new ContractContext();
        contractContext.run();
    }
}
