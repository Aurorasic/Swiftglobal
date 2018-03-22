package cn.primeledger.cas.global.common.entity;

import lombok.Data;
/**
 * @author yangyi
 * @date 2018/2/28
 */
@Data
public class MulticastMessageEntity extends StringMessageEntity {
    private String[] includeSourceIds;
    //todo yangyi change this parameter to map
    private long height;
}
