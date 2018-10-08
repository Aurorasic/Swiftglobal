package com.higgsblock.global.chain.vm.config;

import com.higgsblock.global.chain.vm.core.SystemProperties;
import org.springframework.stereotype.Component;

/**
 * @author Chen Jiawei
 * @date 2018-09-29
 */
@Component
public class DefaultSystemProperties extends SystemProperties {
    @Override
    public boolean vmTrace() {
        return false;
    }

    @Override
    public int dumpBlock() {
        return 1898;
    }

    @Override
    public String dumpStyle() {
        return "pretty";
    }
}
