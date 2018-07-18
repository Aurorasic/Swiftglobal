package com.higgsblock.global.chain.app.common.message;

import com.higgsblock.global.chain.app.common.constants.EntityType;

import java.lang.annotation.*;

/**
 * @author baizhengwen
 * @date 2018-05-04
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Message {
    EntityType value();
}
