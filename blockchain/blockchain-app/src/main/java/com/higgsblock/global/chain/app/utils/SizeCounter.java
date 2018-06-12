package com.higgsblock.global.chain.app.utils;

/**
 * @author chenjiawei
 * @date 2018-03-28
 */
public interface SizeCounter {
    /**
     * Calculate size of an object.
     *
     * @param o object to calculate
     * @return size of object
     */
    long calculateSize(Object o);
}
