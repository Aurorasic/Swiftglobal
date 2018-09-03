package com.higgsblock.global.chain.app.sync.handler;

import com.higgsblock.global.chain.app.blockchain.IBlockChainService;
import com.higgsblock.global.chain.app.blockchain.listener.MessageCenter;
import com.higgsblock.global.chain.app.common.handler.BaseMessageHandler;
import com.higgsblock.global.chain.network.socket.message.IMessage;
import com.higgsblock.global.chain.app.sync.message.MaxHeightRequest;
import com.higgsblock.global.chain.app.sync.message.MaxHeightResponse;
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
    private IBlockChainService blockChainService;

    @Autowired
    private MessageCenter messageCenter;

    @Override
    protected boolean valid(IMessage<MaxHeightRequest> message) {
        MaxHeightRequest data = message.getData();
        return null != data;
    }

    @Override
    protected void process(IMessage<MaxHeightRequest> message) {
        messageCenter.unicast(message.getSourceId(), new MaxHeightResponse(blockChainService.getMaxHeight()));
    }
}
