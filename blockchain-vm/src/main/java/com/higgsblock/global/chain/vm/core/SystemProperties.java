package com.higgsblock.global.chain.vm.core;

import com.higgsblock.global.chain.vm.DataWord;
import com.higgsblock.global.chain.vm.GasCost;
import com.higgsblock.global.chain.vm.OpCode;
import com.higgsblock.global.chain.vm.program.Program;

/**
 * @author tangkun
 * @date 2018-09-06
 */
public class SystemProperties {

    private static SystemProperties CONFIG;
    private static boolean useOnlySpringConfig = false;
    private static final GasCost GAS_COST = new GasCost();

    /**
     * Returns the static config instance. If the config is passed
     * as a Spring bean by the application this instance shouldn't
     * be used
     * This method is mainly used for testing purposes
     * (Autowired fields are initialized with this static instance
     * but when running within Spring context they replaced with the
     * bean config instance)
     */
    public static SystemProperties getDefault() {
        return useOnlySpringConfig ? null : getSpringDefault();
    }

    static SystemProperties getSpringDefault() {
        if (CONFIG == null) {
            CONFIG = new SystemProperties();
        }
        return CONFIG;
    }


    public String vmTraceDir() {
        return "vm.structured.dir";
    }

    public boolean vmTrace() {
        return false;
    }

    public String databaseDir() {
        return "database.dir";
    }

    public boolean playVM() {
        return true;
    }

    public static GasCost getGasCost() {
        return GAS_COST;
    }

    public DataWord getCallGas(OpCode op, DataWord requestedGas, DataWord availableGas) throws Program.OutOfGasException {
        if (requestedGas.compareTo(availableGas) > 0) {
            throw Program.Exception.notEnoughOpGas(op, requestedGas, availableGas);
        }
        return requestedGas.clone();
    }

    public String getCryptoProviderName() {
        return "SC";
    }

    public String getHash256AlgName() {
        return "ETH-KECCAK-256";
    }

    public String getHash512AlgName() {
        return "ETH-KECCAK-512";
    }
}
