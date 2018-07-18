package com.higgsblock.global.chain.app.sync;

import com.higgsblock.global.chain.app.blockchain.BlockProcessor;
import com.higgsblock.global.chain.app.blockchain.listener.MessageCenter;
import com.higgsblock.global.chain.app.common.SocketRequest;
import com.higgsblock.global.chain.app.common.handler.BaseMessageHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @author yuanjiantao
 * @date 3/8/2018
 */
@Component
@Slf4j
public class MaxHeightRequestHandler extends BaseMessageHandler<MaxHeightRequest> {

    @Autowired
    private BlockProcessor blockProcessor;

    @Autowired
    private MessageCenter messageCenter;

    @Override
    protected void process(SocketRequest<MaxHeightRequest> request) {
        messageCenter.unicast(request.getSourceId(), new MaxHeightResponse(blockProcessor.getMaxHeight()));
    }
}
