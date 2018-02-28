package cn.primeledger.cas.global.config;

/**
 * Define sorts of network types for the p2p network.
 *
 * @author  zhao xiaogang
 * */
public enum  NetworkType {
    MAINNET((byte) 0, "mainnet"),

    TESTNET((byte) 1, "testnet"),

    DEVNET((byte) 2, "devnet");

    byte type;
    String desc;

    NetworkType(byte type, String desc) {
        this.type = type;
        this.desc = desc;
    }
}
