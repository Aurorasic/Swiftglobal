package com.higgsblock.global.chain.app.consensus.syncblock;

import com.higgsblock.global.chain.app.blockchain.Block;
import com.higgsblock.global.chain.app.blockchain.BlockService;
import com.higgsblock.global.chain.app.blockchain.listener.MessageCenter;
import com.higgsblock.global.chain.app.common.SocketRequest;
import com.higgsblock.global.chain.app.common.handler.BaseEntityHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @author yuanjiantao
 * @date Created on 3/8/2018
 */
@Component("getDataHandler")
@Slf4j
public class GetDataHandler extends BaseEntityHandler<GetData> {

    @Autowired
    private BlockService blockService;

    @Autowired
    private MessageCenter messageCenter;

    @Override
    protected void process(SocketRequest<GetData> request) {
        GetData data = request.getData();
        long height = data.getHeight();
        if (height <= 0L) {
            return;
        }
        String sourceId = request.getSourceId();
        String hash = data.getHash();
        /**
         * send block to peer
         */
        if (null != hash) {
            Block block = blockService.getBlock(hash);
            if (null != block) {
                messageCenter.unicast(sourceId, block);
            }
        } else {
            blockService.getBlocksByHeight(height).forEach(block -> messageCenter.unicast(sourceId, block));
        }
    }
}

