package com.higgsblock.global.chain.app.keyvalue.annotation;

import org.springframework.data.annotation.QueryAnnotation;

import java.lang.annotation.*;

/**
 * @author baizhengwen
 * @date 2018-08-30
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD, ElementType.ANNOTATION_TYPE})
@QueryAnnotation
@Documented
public @interface IndexQuery {
    String value();
}
