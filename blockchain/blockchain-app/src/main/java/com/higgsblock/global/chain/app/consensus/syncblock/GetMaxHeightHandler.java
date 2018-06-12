package com.higgsblock.global.chain.app.consensus.syncblock;

import com.higgsblock.global.chain.app.blockchain.BlockService;
import com.higgsblock.global.chain.app.blockchain.listener.MessageCenter;
import com.higgsblock.global.chain.app.common.SocketRequest;
import com.higgsblock.global.chain.app.common.handler.BaseEntityHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @author yuanjiantao
 * @date 3/8/2018
 */
@Component("getMaxHeightHandler")
@Slf4j
public class GetMaxHeightHandler extends BaseEntityHandler<GetMaxHeight> {

    @Autowired
    private BlockService blockService;

    @Autowired
    private MessageCenter messageCenter;

    @Override
    protected void process(SocketRequest<GetMaxHeight> request) {
        messageCenter.unicast(request.getSourceId(), new MaxHeight(blockService.getBestMaxHeight()));
    }
}
