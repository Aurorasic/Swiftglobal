package com.higgsblock.global.chain.app.blockchain;

import com.higgsblock.global.chain.app.common.SystemStatusManager;
import com.higgsblock.global.chain.app.common.SystemStepEnum;
import com.higgsblock.global.chain.app.dao.*;
import com.higgsblock.global.chain.app.service.IBlockService;
import com.higgsblock.global.chain.app.sync.SyncBlockProcessor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author yuanjiantao
 * @date 5/21/2018
 */
@Service
@Slf4j
public class DataErrorProcessor {

    @Autowired
    private SystemStatusManager systemStatusManager;

    @Autowired
    private IBlockService blockService;

    @Autowired
    private BlockProcessor blockProcessor;

    @Autowired
    private IBlockRepository blockRepository;

    @Autowired
    private IBlockIndexRepository blockIndexRepository;

    @Autowired
    private IDposRepository dposRepository;

    @Autowired
    private IScoreRepository scoreRepository;

    @Autowired
    private IUTXORepository utxoRepository;

    @Autowired
    private ITransactionIndexRepository transactionIndexRepository;

    @Autowired
    private SyncBlockProcessor syncBlockProcessor;

    /**
     * delete data except block and witness
     */
    private void deleteData() {
        dposRepository.deleteAll();
        scoreRepository.deleteAll();
        utxoRepository.deleteAll();
        transactionIndexRepository.deleteAll();
    }

    private void reimportData() {

        long maxHeight = blockIndexRepository.queryMaxHeight();
        long startDeleteHeight = 2L;
        for (long height = 2L; height < maxHeight; height += 1L) {
            List<Block> list = blockService.getBlocksByHeight(height);
            if (CollectionUtils.isEmpty(list)) {
                startDeleteHeight = height;
                LOGGER.info("stop reimport data ,current hheight={},max height={}", height, maxHeight);
                break;
            }
            blockIndexRepository.deleteByHeight(height);
            blockRepository.deleteByHeight(height);
            list.forEach(block -> blockProcessor.persistBlockAndIndex(block, null, block.getVersion()));
            if (CollectionUtils.isEmpty(blockService.getBlocksByHeight(height))) {
                startDeleteHeight = height;
                LOGGER.info("stop reimport data ,current hheight={},max height={}", height, maxHeight);
                break;
            }
        }

        for (long height = startDeleteHeight; height < maxHeight; height += 1L) {
            blockIndexRepository.deleteByHeight(height);
            blockRepository.deleteByHeight(height);
        }
    }

    public void handleError() {

        LOGGER.info("start reimport data");

        systemStatusManager.setSysStep(SystemStepEnum.START_CHECK_DATA);
        try {
            Thread.sleep(10000);
        } catch (InterruptedException e) {
            LOGGER.error(e.getMessage(), e);
        }
        // delete data except block and blockIndex
        deleteData();

        reimportData();

        //load all data
        blockProcessor.loadAllBlockData();
        systemStatusManager.setSysStep(SystemStepEnum.LOADED_ALL_DATA);

        //start sync block
        syncBlockProcessor.startSyncBlock();
    }

}
