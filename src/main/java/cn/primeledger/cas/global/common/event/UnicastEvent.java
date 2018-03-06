package cn.primeledger.cas.global.common.event;

import cn.primeledger.cas.global.common.entity.UnicastMessageEntity;
import lombok.Data;

/**
 * @author baizhengwen
 * @date 2018/2/28
 */
@Data
public class UnicastEvent {

    private UnicastMessageEntity entity;

}
