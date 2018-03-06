package cn.primeledger.cas.global.common.event;

import cn.primeledger.cas.global.common.entity.BroadcastMessageEntity;
import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * @author baizhengwen
 * @date 2018/2/28
 */
@Data
@AllArgsConstructor
public class BroadcastEvent {

    private BroadcastMessageEntity entity;

}
