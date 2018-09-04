package com.higgsblock.global.chain.app.keyvalue.aop;

import com.higgsblock.global.chain.app.keyvalue.annotation.Transactional;
import com.higgsblock.global.chain.app.keyvalue.core.ITransactionAwareKeyValueAdapter;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.lang.reflect.Method;

/**
 * @author baizhengwen
 * @date 2018-08-27
 */
@Slf4j
@Aspect
@Component
public class KeyValueTransactionSupporter {

    @Resource(name = "keyValueAdapter")
    private ITransactionAwareKeyValueAdapter adapter;

    @Pointcut("execution (* com.higgsblock.global.chain.app..*.*(..))")
    public void aspect() {
    }

    @Around("aspect()")
    public Object around(ProceedingJoinPoint point) throws Throwable {
        Object obj = null;
        Method method = ((MethodSignature) point.getSignature()).getMethod();
        Transactional transactional = method.getAnnotation(Transactional.class);
        if (null == transactional) {
            return point.proceed(point.getArgs());
        }

        if (null != transactional) {
            try {
                adapter.beginTransaction();
                LOGGER.debug("start transaction");
                obj = point.proceed(point.getArgs());
                adapter.commitTransaction();
                LOGGER.debug("commit transaction");
            } catch (Throwable e) {
                LOGGER.error(e.getMessage(), e);
                adapter.rollbackTransaction();
                throw e;
            }
        }

        return obj;
    }
}
