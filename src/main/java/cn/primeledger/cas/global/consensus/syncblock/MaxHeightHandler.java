package cn.primeledger.cas.global.consensus.syncblock;

import cn.primeledger.cas.global.blockchain.BlockService;
import cn.primeledger.cas.global.blockchain.listener.MessageCenter;
import cn.primeledger.cas.global.common.SocketRequest;
import cn.primeledger.cas.global.common.handler.BaseEntityHandler;
import cn.primeledger.cas.global.constants.EntityType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @author yuanjiantao
 * @date Created on 3/8/2018
 */
@Component("maxHeightHandler")
@Slf4j
public class MaxHeightHandler extends BaseEntityHandler<MaxHeight> {

    @Autowired
    private BlockService blockService;

    @Autowired
    private SyncBlockService syncBlockService;

    @Autowired
    private MessageCenter messageCenter;

    @Override
    public EntityType getType() {
        return EntityType.MAXHEIGHT;
    }

    @Override
    protected void process(SocketRequest<MaxHeight> request) {
        MaxHeight maxHeight = request.getData();
        syncBlockService.updatePeersMaxHeight(maxHeight.getMaxHeight(), request.getSourceId());
    }
}
