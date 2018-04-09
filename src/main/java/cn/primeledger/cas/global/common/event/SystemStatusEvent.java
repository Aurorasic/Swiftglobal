package cn.primeledger.cas.global.common.event;

import cn.primeledger.cas.global.common.SystemStatus;
import cn.primeledger.cas.global.entity.BaseSerializer;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author yuguojia
 * @date 2018/04/03
 **/
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SystemStatusEvent extends BaseSerializer {
    private SystemStatus systemStatus;
}