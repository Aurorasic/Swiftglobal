package com.higgsblock.global.chain.app.task;

import com.higgsblock.global.chain.app.blockchain.listener.MessageCenter;
import com.higgsblock.global.chain.app.sync.message.MaxHeightRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

/**
 * @author baizhengwen
 * @date 2018-07-27
 */
@Component
public class GetMaxHeightTask extends BaseTask {

    @Autowired
    private MessageCenter messageCenter;

    @Override
    protected void task() {
        MaxHeightRequest request = new MaxHeightRequest();
        messageCenter.broadcast(request);
    }

    @Override
    protected long getPeriodMs() {
        return TimeUnit.SECONDS.toMillis(10);
    }
}
