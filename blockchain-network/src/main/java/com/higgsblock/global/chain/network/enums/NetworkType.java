package com.higgsblock.global.chain.network.enums;

/**
 * Define sorts of network types for the p2p network.
 *
 * @author zhao xiaogang
 */
public enum NetworkType {
    /**
     * Main net network type.
     */
    MAIN_NET((byte) 0, "main net"),

    /**
     * Test net network type.
     */
    TEST_NET((byte) 1, "test net"),

    /**
     * Dev net network type.
     */
    DEV_NET((byte) 2, "dev net");

    /**
     * The Type.
     */
    byte type;
    /**
     * The Desc.
     */
    String desc;

    /**
     * Instantiates a new Network type.
     *
     * @param type the type
     * @param desc the desc
     */
    NetworkType(byte type, String desc) {
        this.type = type;
        this.desc = desc;
    }

    /**
     * Gets type.
     *
     * @return the type
     */
    public byte getType() {
        return type;
    }

    /**
     * Get network type network type.
     *
     * @param networkType the network type
     * @return the network type
     */
    public static NetworkType getNetworkType(byte networkType){
        for(NetworkType item: NetworkType.values()){
            if (item.type == networkType) {
                return item;
            }
        }

        return NetworkType.DEV_NET;
    }
}
