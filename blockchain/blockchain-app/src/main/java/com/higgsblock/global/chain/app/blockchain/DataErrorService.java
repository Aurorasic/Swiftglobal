package com.higgsblock.global.chain.app.blockchain;

import com.google.common.eventbus.EventBus;
import com.higgsblock.global.chain.app.common.SystemStatusManager;
import com.higgsblock.global.chain.app.common.SystemStepEnum;
import com.higgsblock.global.chain.app.consensus.syncblock.SyncBlockService;
import com.higgsblock.global.chain.app.dao.*;
import lombok.extern.slf4j.Slf4j;
import org.rocksdb.RocksDBException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author yuanjiantao
 * @date 5/21/2018
 */
@Service
@Slf4j
public class DataErrorService {

    @Autowired
    private SystemStatusManager systemStatusManager;

    @Autowired
    private BlockService blockService;

    @Autowired
    private EventBus eventBus;

    @Autowired
    private SyncBlockService syncBlockService;

    @Autowired
    private BlockDao blockDao;

    @Autowired
    private BlockIndexDao blockIndexDao;

    @Autowired
    private BlockIndexBakDao blockIndexBakDao;

    @Autowired
    private TransDao transDao;

    @Autowired
    private UtxoDao utxoDao;

    @Autowired
    private WitnessBlockDao witnessBlockDao;

    @Autowired
    private ScoreDao scoreDao;

    @Autowired
    private LatestBlockIndexDao latestBlockIndexDao;


    public void handleError() {

        systemStatusManager.setSysStep(SystemStepEnum.START_CHECK_DATA);

        try {

            long height = 1L;
            BlockIndex blockIndex;
            String hash;
            while (null != (blockIndex = blockIndexDao.get(height))) {
                blockIndexBakDao.writeBatch(blockIndexBakDao.getEntity(height, blockIndex));
                height++;
            }

            //delete data except block
            blockIndexDao.deleteAll();
            transDao.deleteAll();
            utxoDao.deleteAll();
            witnessBlockDao.deleteAll();
            scoreDao.deleteAll();

            //compute data(utxo,score...) from block
            height = 1L;
            while (null != (blockIndex = blockIndexBakDao.get(height))
                    && (null != (hash = blockIndex.getBestBlockHash()))) {
                Block block = blockDao.get(hash);
                if (null == block || !blockService.persistBlockAndIndex(block, null, block.getVersion())) {
                    break;
                }
                height++;
            }

            //delete the rest block
            while (null != (blockIndex = blockIndexBakDao.get(height))
                    && (null != (hash = blockIndex.getBestBlockHash()))) {
                blockDao.writeBatch(blockDao.getEntity(hash, null));
                height++;
            }

            //delete the blockIndexBak
            while (height > 0L) {
                blockIndexBakDao.writeBatch(blockIndexBakDao.getEntity(height, null));
                height--;
            }


        } catch (RocksDBException e) {
            LOGGER.error(e.getMessage(), e);
        }

        //load all data
        blockService.loadAllBlockData();
        systemStatusManager.setSysStep(SystemStepEnum.LOADED_ALL_DATA);

        //start sync block
        syncBlockService.startSyncBlock();
    }

}
