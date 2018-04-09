package cn.primeledger.cas.global.common;

import cn.primeledger.cas.global.blockchain.listener.MessageCenter;
import cn.primeledger.cas.global.common.event.SystemStatusEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @author yuguojia
 * @date 2018/04/03
 **/
@Slf4j
@Component
public class SystemStatusManager {
    @Autowired
    private MessageCenter messageCenter;

    private SystemStatus systemStatus = SystemStatus.INI;

    public void setSysStep(SystemStepEnum sysStep) {
        //todo yuguojia check the status validity
        if (SystemStepEnum.LOADED_ALL_DATA.equals(sysStep)) {
            systemStatus = SystemStatus.SYNC_BLOCKS;
        }
        if (SystemStepEnum.SYNCED_BLOCKS.equals(sysStep)) {
            systemStatus = SystemStatus.RUNNING;
        }
        sendSystemStateEvent(systemStatus);
    }

    private void sendSystemStateEvent(SystemStatus systemStatus) {
        SystemStatusEvent systemStatusEvent = new SystemStatusEvent();
        systemStatusEvent.setSystemStatus(systemStatus);
        messageCenter.send(systemStatusEvent);
        LOGGER.info("sent system status:" + systemStatus);
    }

    public SystemStatus getSystemStatus() {
        return systemStatus;
    }

}