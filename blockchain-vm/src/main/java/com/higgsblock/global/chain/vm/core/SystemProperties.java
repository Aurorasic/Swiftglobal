package com.higgsblock.global.chain.vm.core;

/**
 * @author tangkun
 * @date 2018-09-06
 */
public class SystemProperties {

    private static SystemProperties CONFIG;
    private static boolean useOnlySpringConfig = false;

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

}
