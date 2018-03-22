package cn.primeledger.cas.global.common.event;

import cn.primeledger.cas.global.common.entity.MulticastMessageEntity;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class CollectSignEvent {
    private MulticastMessageEntity entity;
}
