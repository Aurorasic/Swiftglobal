package cn.primeledger.cas.global.common.entity;

import lombok.Data;

/**
 * @author baizhengwen
 * @date 2018/2/28
 */
@Data
public abstract class BaseMessageEntity<T> {
    protected String sourceId;
    protected T data;
}
