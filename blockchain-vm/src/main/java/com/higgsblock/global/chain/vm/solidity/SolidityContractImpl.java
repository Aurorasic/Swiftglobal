package com.higgsblock.global.chain.vm.solidity;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

public class SolidityContractImpl implements SolidityContract {

    byte[] address;
    public CallTransaction.Contract contract;
    public List<CallTransaction.Contract> relatedContracts = new ArrayList<>();

    public SolidityContractImpl(String abi) {
        contract = new CallTransaction.Contract(abi);
    }

    @Override
    public SolidityCallResult callFunction(String functionName, Object... args) {
        CallTransaction.Function function = contract.getByName(functionName);
        SolidityCallResult res = new SolidityCallResultImpl(this, function);
        return res;
    }

    @Override
    public SolidityCallResult callFunction(long value, String functionName, Object... args) {
        return null;
    }

    @Override
    public Object[] callConstFunction(String functionName, Object... args) {
        return new Object[0];
    }

    @Override
    public SolidityFunction getFunction(String name) {
        return null;
    }

    @Override
    public String getABI() {
        return null;
    }

    @Override
    public byte[] getAddress() {
        return new byte[0];
    }

    @Override
    public void call(byte[] callData) {

    }

    @Override
    public String getBinary() {
        return null;
    }
}
