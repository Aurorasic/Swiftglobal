package com.higgsblock.global.chain.app.sync.handler;

import com.higgsblock.global.chain.app.common.handler.BaseMessageHandler;
import com.higgsblock.global.chain.app.sync.SyncBlockInSyncService;
import com.higgsblock.global.chain.app.sync.message.MaxHeightResponse;
import com.higgsblock.global.chain.network.socket.message.IMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @author yuanjiantao
 * @date 3/8/2018
 */
@Component
@Slf4j
public class MaxHeightResponseHandler extends BaseMessageHandler<MaxHeightResponse> {

    @Autowired
    private SyncBlockInSyncService syncBlockInSyncService;

    @Override
    protected boolean valid(IMessage<MaxHeightResponse> message) {
        MaxHeightResponse maxHeightResponse = message.getData();
        return null != maxHeightResponse && maxHeightResponse.valid();
    }

    @Override
    protected void process(IMessage<MaxHeightResponse> message) {
        MaxHeightResponse maxHeightResponse = message.getData();
        syncBlockInSyncService.updatePeersMaxHeight(maxHeightResponse.getMaxHeight(), message.getSourceId());
    }
}
