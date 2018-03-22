package cn.primeledger.cas.global.consensus.syncblock;

import cn.primeledger.cas.global.blockchain.BlockIndex;
import cn.primeledger.cas.global.blockchain.BlockService;
import cn.primeledger.cas.global.common.handler.UniqueEntityHandler;
import cn.primeledger.cas.global.constants.EntityType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashSet;

/**
 * @author yuanjiantao
 * @date Created on 3/8/2018
 */
@Component("maxHeightHandler")
@Slf4j
public class MaxHeightHandler extends UniqueEntityHandler<MaxHeight> {

    @Autowired
    private BlockService blockService;

    @Autowired
    private SyncBlockService syncBlockService;

    @Override
    public EntityType getType() {
        return EntityType.MAXHEIGHT;
    }

    @Override
    public void process(MaxHeight data, short version, String sourceId) {

        BlockIndex blockIndex = blockService.getLastBlockIndex();
        if (null == blockIndex) {
            return;
        }
        Inventory inventory = new Inventory();
        inventory.setHeight(blockIndex.getHeight());
        inventory.setHashs(new HashSet<>(blockIndex.getBlockHashs()));
        syncBlockService.unicastInventory(inventory, sourceId);
    }


}
