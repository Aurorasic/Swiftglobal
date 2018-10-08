package com.higgsblock.global.chain.app.blockchain;

import com.higgsblock.global.chain.app.blockchain.listener.MessageCenter;
import com.higgsblock.global.chain.app.common.SystemStatusManager;
import com.higgsblock.global.chain.app.common.SystemStepEnum;
import com.higgsblock.global.chain.app.dao.IDposRepository;
import com.higgsblock.global.chain.app.dao.ITransactionIndexRepository;
import com.higgsblock.global.chain.app.dao.IUTXORepository;
import com.higgsblock.global.chain.app.service.IBlockChainInfoService;
import com.higgsblock.global.chain.app.service.IBlockIndexService;
import com.higgsblock.global.chain.app.service.IBlockService;
import com.higgsblock.global.chain.app.sync.SyncBlockInStartupService;
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
public class DataErrorService {

    @Autowired
    private SystemStatusManager systemStatusManager;

    @Autowired
    private IBlockService blockService;

    @Autowired
    private IBlockIndexService blockIndexService;

    @Autowired
    private IBlockChainInfoService blockChainInfoService;

    @Autowired
    private IDposRepository dposRepository;

    @Autowired
    private IUTXORepository utxoRepository;

    @Autowired
    private ITransactionIndexRepository transactionIndexRepository;

    @Autowired
    private SyncBlockInStartupService syncBlockInStartupService;

    @Autowired
    private MessageCenter messageCenter;

    /**
     * delete data except block and witness
     */
    private void deleteData() {
        dposRepository.deleteAll();
        blockChainInfoService.deleteAllScores();
        utxoRepository.deleteAll();
        transactionIndexRepository.deleteAll();
    }

    private void reimportData() {
        long maxHeight = blockChainInfoService.getMaxHeight();
        long startDeleteHeight = 2L;
        for (long height = 2L; height < maxHeight; height += 1L) {
            List<Block> list = blockService.getBlocksByHeight(height);
            if (CollectionUtils.isEmpty(list)) {
                startDeleteHeight = height;
                LOGGER.info("stop reimport data ,current hheight={},max height={}", height, maxHeight);
                break;
            }
            blockIndexService.deleteByHeight(height);
            blockService.deleteByHeight(height);
            list.forEach(block -> messageCenter.dispatch(block));
            if (CollectionUtils.isEmpty(blockService.getBlocksByHeight(height))) {
                startDeleteHeight = height;
                LOGGER.info("stop reimport data ,current hheight={},max height={}", height, maxHeight);
                break;
            }
        }

        for (long height = startDeleteHeight; height < maxHeight; height += 1L) {
            blockIndexService.deleteByHeight(height);
            blockService.deleteByHeight(height);
        }
    }

    public void handleError() {

        LOGGER.info("start reimport data");

        systemStatusManager.setSysStep(SystemStepEnum.CHECK_DATA);
        try {
            Thread.sleep(10000);
        } catch (InterruptedException e) {
            LOGGER.error(e.getMessage(), e);
        }
        // delete data except block and blockIndex
        deleteData();

        reimportData();

        //load all data
        // TODO: yuanjiantao 7/22/2018  blockProcessor.loadAllBlockData();
        systemStatusManager.setSysStep(SystemStepEnum.LOADED_ALL_DATA);

        //start sync block
        syncBlockInStartupService.startSyncBlock();
    }

}
