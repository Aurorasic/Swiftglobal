package com.higgsblock.global.chain.app.sync;

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
public class MaxHeightHandler extends BaseEntityHandler<MaxHeightResp> {

    @Autowired
    private SyncBlockService syncBlockService;

    @Override
    protected void process(SocketRequest<MaxHeightResp> request) {
        MaxHeightResp maxHeightResp = request.getData();
        syncBlockService.updatePeersMaxHeight(maxHeightResp.getMaxHeight(), request.getSourceId());
    }
}
