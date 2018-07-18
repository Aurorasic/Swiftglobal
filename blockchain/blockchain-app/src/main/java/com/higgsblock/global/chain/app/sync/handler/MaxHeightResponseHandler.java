package com.higgsblock.global.chain.app.sync.handler;

import com.higgsblock.global.chain.app.common.SocketRequest;
import com.higgsblock.global.chain.app.common.handler.BaseMessageHandler;
import com.higgsblock.global.chain.app.sync.message.MaxHeightResponse;
import com.higgsblock.global.chain.app.sync.SyncBlockProcessor;
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
    private SyncBlockProcessor syncBlockProcessor;

    @Override
    protected void process(SocketRequest<MaxHeightResponse> request) {
        MaxHeightResponse maxHeightResponse = request.getData();
        syncBlockProcessor.updatePeersMaxHeight(maxHeightResponse.getMaxHeight(), request.getSourceId());
    }
}
