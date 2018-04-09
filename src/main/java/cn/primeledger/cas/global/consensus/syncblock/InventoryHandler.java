package cn.primeledger.cas.global.consensus.syncblock;

import cn.primeledger.cas.global.blockchain.BlockIndex;
import cn.primeledger.cas.global.blockchain.BlockService;
import cn.primeledger.cas.global.blockchain.listener.MessageCenter;
import cn.primeledger.cas.global.common.SocketRequest;
import cn.primeledger.cas.global.common.handler.BaseEntityHandler;
import cn.primeledger.cas.global.constants.EntityType;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;

/**
 * @author yuanjiantao
 * @date Created on 3/8/2018
 */
@Component("inventoryHandler")
@Slf4j
public class InventoryHandler extends BaseEntityHandler<Inventory> {

    @Autowired
    private BlockService blockService;

    @Autowired
    private SyncBlockService syncBlockService;

    @Autowired
    private MessageCenter messageCenter;

    @Override
    public EntityType getType() {
        return EntityType.INVENTORY;
    }

    @Override
    protected void process(SocketRequest<Inventory> request) {
        Inventory data = request.getData();
        String sourceId = request.getSourceId();
        long maxHeight = data.getHeight();

        BlockIndex blockIndex = blockService.getBlockIndexByHeight(maxHeight);
        if (null != blockIndex) {
            List<String> myHashs = blockIndex.getBlockHashs();
            Set<String> peerHashs = data.getHashs();
            LOGGER.info("process inventory message! height:{}  ;myhashs:{}  peerHashs:{}", maxHeight, myHashs, peerHashs);

            /**
             * send block to peer
             */
            if (CollectionUtils.isNotEmpty(myHashs)) {
                if (CollectionUtils.isNotEmpty(peerHashs)) {
                    myHashs.removeAll(peerHashs);
                }

                myHashs.forEach(hash -> {
                    messageCenter.unicast(sourceId, blockService.getBlock(hash));
                });
            }
            /**
             * send sync block request to peer
             */
            if (CollectionUtils.isNotEmpty(peerHashs)) {
                myHashs = blockIndex.getBlockHashs();
                if (CollectionUtils.isNotEmpty(myHashs)) {
                    peerHashs.removeAll(myHashs);
                }
                if (CollectionUtils.isNotEmpty(peerHashs)) {
                    syncBlockService.syncBlockByHeight(maxHeight, sourceId);
                }
            }
            return;
        }
        /**
         * send sync block request to peer
         */
        if (CollectionUtils.isNotEmpty(data.getHashs())) {
            syncBlockService.syncBlockByHeight(maxHeight, sourceId);
        }
    }
}

