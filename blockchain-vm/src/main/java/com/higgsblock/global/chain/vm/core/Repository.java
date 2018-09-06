package com.higgsblock.global.chain.vm.core;

/**
 * @author tangkun
 * @date 2018-09-06
 */
public interface Repository {
    /**
     * @param addr - account to check
     * @return - true if account exist,
     *           false otherwise
     */
    boolean isExist(byte[] addr);

}
