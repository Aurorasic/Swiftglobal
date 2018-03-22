package cn.primeledger.cas.global.entity;

import lombok.Data;

/**
 * @author baizhengwen
 * @date 2018/3/8
 */
@Data
public abstract class BaseBizEntity extends BaseSerializer {
    protected short version;
}
