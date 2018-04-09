package cn.primeledger.cas.global.blockchain.handler;

import cn.primeledger.cas.global.blockchain.Block;
import cn.primeledger.cas.global.blockchain.BlockCacheManager;
import cn.primeledger.cas.global.blockchain.BlockService;
import cn.primeledger.cas.global.blockchain.listener.MessageCenter;
import cn.primeledger.cas.global.common.SocketRequest;
import cn.primeledger.cas.global.common.handler.BaseEntityHandler;
import cn.primeledger.cas.global.consensus.syncblock.Inventory;
import cn.primeledger.cas.global.constants.EntityType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.Set;

/**
 * @author baizhengwen
 * @date 2018/2/28
 */
@Component("blockHandler")
@Slf4j
public class BlockHandler extends BaseEntityHandler<Block> {

    @Autowired
    private BlockService blockService;

    @Autowired
    private BlockCacheManager blockCacheManager;

    @Autowired
    private MessageCenter messageCenter;

    @Override
    public EntityType getType() {
        return EntityType.BLOCK_BROADCAST;
    }

    @Override
    protected void process(SocketRequest<Block> request) {
        Block data = request.getData();
        long height = data.getHeight();
        String hash = data.getHash();
        if (blockCacheManager.isContains(hash)) {
            return;
        }
        short version = data.getVersion();
        String sourceId = request.getSourceId();

        boolean success = blockService.persistBlockAndIndex(data, sourceId, version);
        LOGGER.info("persisted block all info, height={}_block={},success?{}", height, hash, success);
        if (success && !data.isgenesisBlock()) {
            Inventory inventory = new Inventory();
            inventory.setHeight(height);
            Set<String> set = new HashSet<>(blockService.getBlockIndexByHeight(height).getBlockHashs());
            inventory.setHashs(set);
            messageCenter.broadcast(new String[]{sourceId}, inventory);
//            messageCenter.broadcast(new String[]{sourceId}, data);
        }
    }
}
