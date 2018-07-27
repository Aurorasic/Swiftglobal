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
 *
 */
@Aspect
@Component
@Slf4j
public class EventBusAspect {

    //TODO for test huaangshengli 2018-07-27
    @Around("@annotation(com.google.common.eventbus.Subscribe)")
    public Object logger(ProceedingJoinPoint point) throws Throwable {
        StopWatch watch = new StopWatch();
        watch.start();
        LOGGER.info("event processor start");
        Object[] params = point.getArgs();
        Method method = ((MethodSignature) point.getSignature()).getMethod();
        String className = point.getTarget().getClass().getName();
        String methodName = method.getName();
        Object returnObj = null;
        Object event = null;
        if (params != null && params.length > 0) {
            event = params[0];
        }
        try {
            returnObj = point.proceed();
        } catch (Throwable e) {
            if (event != null) {
                LOGGER.error(String.format("process event[%s],elapsed time:%s Sec,error!", event.getClass().getSimpleName(), watch.getTotalTimeSeconds()), e);
            } else {
                LOGGER.error(String.format("process event[%s->%s],elapsed time:%s Sec,error!", className, methodName, watch.getTotalTimeSeconds()), e);
            }
            throw e;
        }
        if (event != null) {
            LOGGER.info("process event [{}],elapsed time:{} Sec,successfully!", event.getClass().getSimpleName(), watch.getTotalTimeSeconds());
        } else {
            LOGGER.info("process event [{}->{}],elapsed time:{} Sec,successfully!", className, methodName, watch.getTotalTimeSeconds());
        }
        return returnObj;
    }
}
