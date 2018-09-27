package com.higgsblock.global.chain.vm.solidity;

public class SolidityCallResultImpl extends SolidityCallResult {

    SolidityContractImpl contract;
    CallTransaction.Function function;

    SolidityCallResultImpl(SolidityContractImpl contract, CallTransaction.Function function) {
        this.contract = contract;
        this.function = function;
    }

    @Override
    public CallTransaction.Function getFunction() {
        return function;
    }
}
