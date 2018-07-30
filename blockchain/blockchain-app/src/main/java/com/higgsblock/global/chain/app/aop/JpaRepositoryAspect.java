package com.higgsblock.global.chain.app.aop;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;
import org.springframework.util.StopWatch;

import java.lang.reflect.Method;

/**
 * @author huangshengli
 * @since 2018-07-27
 */
@Aspect
@Component
@Slf4j
public class JpaRepositoryAspect {

    //TODO for test huaangshengli 2018-07-27
    @Around("execution(* com.higgsblock.global.chain.app.dao..*.*(..))")
    public Object logger(ProceedingJoinPoint point) throws Throwable {
        StopWatch watch = new StopWatch();
        watch.start();
        LOGGER.info("dao operate start");
        Object[] params = point.getArgs();
        Method method = ((MethodSignature) point.getSignature()).getMethod();
        String className = point.getTarget().getClass().getName();
        String methodName = method.getName();
        Object returnObj;

        try {
            returnObj = point.proceed();
            watch.stop();
        } catch (Throwable e) {
            LOGGER.error(String.format("dao operate[%s->%s,params:%s],elapsed time:%s ms,error!", className, methodName, params, watch.getTotalTimeMillis()), e);
            throw e;
        }
        LOGGER.info("dao operate [{}->{},params:{}],elapsed time:{} ms,successfully!", className, methodName, params, watch.getTotalTimeMillis());
        return returnObj;
    }
}
