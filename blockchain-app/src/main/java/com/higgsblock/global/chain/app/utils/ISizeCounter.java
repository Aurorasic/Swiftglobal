package com.higgsblock.global.chain.app.utils;

/**
 * Provide a facade to calculate size of an object.
 *
 * @author chenjiawei
 * @date 2018-03-28
 */
public interface ISizeCounter {
    /**
     * Calculate size of an object.
     *
     * @param o object to calculate
     * @return size of object
     */
    long calculateSize(Object o);
}
