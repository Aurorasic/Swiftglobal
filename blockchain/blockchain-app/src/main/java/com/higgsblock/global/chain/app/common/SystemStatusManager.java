package com.higgsblock.global.chain.app.common;

import com.google.common.eventbus.EventBus;
import com.higgsblock.global.chain.app.common.event.SystemStatusEvent;
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
    private EventBus eventBus;

    private SystemStatus systemStatus = SystemStatus.INI;

    public void setSysStep(SystemStepEnum sysStep) {
        //todo yuguojia 2018-6-4 check the status validity
        if (SystemStepEnum.LOADED_ALL_DATA.equals(sysStep)) {
            systemStatus = SystemStatus.SYNC_BLOCKS;
        }
        if (SystemStepEnum.SYNCED_BLOCKS.equals(sysStep)) {
            systemStatus = SystemStatus.RUNNING;
        }
        if (SystemStepEnum.START_CHECK_DATA.equals(sysStep)) {
            systemStatus = SystemStatus.LOADING;
        }
        sendSystemStateEvent(systemStatus);
    }

    private void sendSystemStateEvent(SystemStatus systemStatus) {
        SystemStatusEvent systemStatusEvent = new SystemStatusEvent();
        systemStatusEvent.setSystemStatus(systemStatus);
        eventBus.post(systemStatusEvent);
        LOGGER.info("sent system status: {}", systemStatus);
    }

    public SystemStatus getSystemStatus() {
        return systemStatus;
    }

}