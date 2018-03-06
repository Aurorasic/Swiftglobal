package cn.primeledger.cas.global.common.event;

import cn.primeledger.cas.global.common.entity.CollectSignMessageEntity;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class CollectSignEvent {
    private CollectSignMessageEntity entity;
}
