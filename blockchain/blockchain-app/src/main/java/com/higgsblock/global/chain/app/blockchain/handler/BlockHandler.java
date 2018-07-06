package com.higgsblock.global.chain.app.blockchain.handler;

import com.higgsblock.global.chain.app.blockchain.*;
import com.higgsblock.global.chain.app.blockchain.listener.MessageCenter;
import com.higgsblock.global.chain.app.common.SocketRequest;
import com.higgsblock.global.chain.app.common.handler.BaseEntityHandler;
import com.higgsblock.global.chain.app.consensus.sign.service.WitnessService;
import com.higgsblock.global.chain.app.consensus.syncblock.Inventory;
import com.higgsblock.global.chain.app.service.impl.BlockIdxDaoService;
import com.higgsblock.global.chain.network.PeerManager;
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
    private OrphanBlockCacheManager orphanBlockCacheManager;

    @Autowired
    private MessageCenter messageCenter;

    @Autowired
    private WitnessService witnessService;

    @Autowired
    private BlockIdxDaoService blockIdxDaoService;

    @Autowired
    private PeerManager peerManager;
    @Autowired
    private CandidateMiner candidateMiner;

    @Override
    protected void process(SocketRequest<Block> request) {
        Block data = request.getData();
        long height = data.getHeight();
        String hash = data.getHash();
        String sourceId = request.getSourceId();

        if (orphanBlockCacheManager.isContains(hash)) {
            return;
        }

        boolean success = blockService.persistBlockAndIndex(data, sourceId, data.getVersion());
        LOGGER.error("persisted block all info, success={}_height={}_block={}", success, height, hash);

        if (success && !data.isGenesisBlock()) {

            String address = peerManager.getSelf().getId();
            //todo yezaiyong 20180630 inform candidateMiner  cout cminer coin
//            CandidateMiner candidateMiner = new CandidateMiner();
            candidateMiner.instantiationBlock();
            //todo yezaiyong 20180630 infom witiness count time
            if (BlockService.WITNESS_ADDRESS_LIST.contains(address)) {
                WitnessCountTime.instantiationBlock(data);
            }

            Inventory inventory = new Inventory();
            inventory.setHeight(height);
            Set<String> set = new HashSet<>(blockIdxDaoService.getBlockIndexByHeight(height).getBlockHashs());
            inventory.setHashs(set);
            messageCenter.broadcast(new String[]{sourceId}, inventory);
            witnessService.initWitnessTask(blockService.getMaxHeight() + 1);
        }
    }
}
