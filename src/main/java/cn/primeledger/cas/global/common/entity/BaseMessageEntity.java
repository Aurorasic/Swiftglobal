package cn.primeledger.cas.global.common.entity;

import lombok.Data;

/**
 * @author baizhengwen
 * @date 2018/2/28
 */
@Data
public abstract class BaseMessageEntity<T> {
    private short type;
    private short version;
    private T data;
    private String sourceId;
}
