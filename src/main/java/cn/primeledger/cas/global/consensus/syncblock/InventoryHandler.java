package cn.primeledger.cas.global.consensus.syncblock;

import cn.primeledger.cas.global.blockchain.Block;
import cn.primeledger.cas.global.blockchain.BlockService;
import cn.primeledger.cas.global.common.handler.UniqueEntityHandler;
import cn.primeledger.cas.global.constants.EntityType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @author yuanjiantao
 * @date Created on 3/8/2018
 */
@Component("inventoryHandler")
@Slf4j
public class InventoryHandler extends UniqueEntityHandler<Inventory> {

    private static final int EACHINVENTORYSIZE = 10;

    @Autowired
    private BlockService blockService;

    @Autowired
    private SyncBlockService syncBlockService;

    @Override
    public EntityType getType() {
        return EntityType.INVENTORY;
    }

    @Override
    public void process(Inventory data, short version, String sourceId) {

        long maxHeight = data.getHeight();
        syncBlockService.updateMaxHeight(maxHeight, sourceId);

        long height = data.getHeight();
        if (height != 0L) {
            Set<String> hashs = data.getHashs();
            List<Block> list = blockService.getBlocksByHeight(height);
            if (null != hashs && hashs.size() > 0) {

                Set<String> myHashs = new HashSet<>();
                list.forEach(block -> myHashs.add(block.getHash()));
                myHashs.removeAll(hashs);
                myHashs.forEach(hash -> {
                    Block block = blockService.getBlock(hash);
                    syncBlockService.sendBlock(block, sourceId);
                });
            } else {
                list.forEach(block -> syncBlockService.sendBlock(block, sourceId));
            }
        }
    }
}
