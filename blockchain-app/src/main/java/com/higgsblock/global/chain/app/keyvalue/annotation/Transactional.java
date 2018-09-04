package com.higgsblock.global.chain.app.keyvalue.annotation;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.*;

/**
 * @author baizhengwen
 * @date 2018-08-24
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(value = {FIELD, METHOD, ANNOTATION_TYPE})
public @interface Transactional {
    Class<? extends Throwable>[] rollbackFor() default {RuntimeException.class};
}
