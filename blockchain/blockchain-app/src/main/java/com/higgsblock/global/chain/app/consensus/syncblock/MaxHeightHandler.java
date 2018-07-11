package com.higgsblock.global.chain.app.consensus.syncblock;

import com.higgsblock.global.chain.app.common.SocketRequest;
import com.higgsblock.global.chain.app.common.handler.BaseEntityHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @author yuanjiantao
 * @date 3/8/2018
 */
@Component("maxHeightHandler")
@Slf4j
public class MaxHeightHandler extends BaseEntityHandler<MaxHeight> {

    @Autowired
    private SyncBlockService syncBlockService;

    @Override
    protected void process(SocketRequest<MaxHeight> request) {
        MaxHeight maxHeight = request.getData();
        syncBlockService.updatePeersMaxHeight(maxHeight.getMaxHeight(), request.getSourceId());
    }
}
