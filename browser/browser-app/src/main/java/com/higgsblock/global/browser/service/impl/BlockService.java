package com.higgsblock.global.browser.service.impl;

import com.google.common.collect.Lists;
import com.higgsblock.global.browser.dao.entity.*;
import com.higgsblock.global.browser.service.bo.BlockBO;
import com.higgsblock.global.browser.service.bo.BlockHeaderBO;
import com.higgsblock.global.browser.service.bo.BlockWitnessBO;
import com.higgsblock.global.browser.service.bo.TransactionBO;
import com.higgsblock.global.browser.service.iface.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author yangshenghong
 * @date 2018-05-24
 */
@Service
@Slf4j
public class BlockService implements IBlockService {

    @Autowired
    private IBlockHeaderService iBlockHeaderService;

    @Autowired
    private ITransactionService iTransactionService;

    @Autowired
    private ITransactionInputService iTransactionInputService;

    @Autowired
    private ITransactionOutputService iTransactionOutputService;

    @Autowired
    private IMinersService iMinersService;

    @Autowired
    private IUTXOService iutxoService;

    @Autowired
    private IRewardService iRewardService;

    @Override
    public BlockBO getBlockByHash(String blockHash) {
        if (StringUtils.isEmpty(blockHash)) {
            LOGGER.error("blockHash is empty");
            return null;
        }

        //query blockHeader by block hash
        BlockHeaderBO blockHeaderBo = iBlockHeaderService.getByField(blockHash);
        if (blockHeaderBo == null) {
            LOGGER.error("The block header is returned empty through a block hash query,blockHash = {}", blockHash);
            return null;
        }

        //According to the block hash gets the block trading hash list
        List<String> txHashByBlockHash = iTransactionService.getTxHashByBlockHash(blockHash);
        if (CollectionUtils.isEmpty(txHashByBlockHash)) {
            LOGGER.error("Query the transaction hash list by block hash and return empty,blockHash = {}", blockHash);
            return null;
        }

        //get the transaction list
        List<TransactionBO> transactionBo = Lists.newArrayList();
        txHashByBlockHash.forEach(txHash -> {
            //query transaction by transaction hash
            TransactionBO transactionByHash = iTransactionService.getTransactionByHash(txHash);
            if (transactionByHash != null) {
                transactionBo.add(transactionByHash);
            } else {
                LOGGER.error("The transaction is returned empty by querying the transaction hash,transactionHash = {}", txHash);
            }
        });

        //Get the blockMiners list
        List<BlockWitnessBO> blockMiners = Lists.newArrayList();
        String minerAddress = blockHeaderBo.getMinerAddress();
        if (!StringUtils.isEmpty(minerAddress)) {
            BlockWitnessBO blockWitnessBo = new BlockWitnessBO();
            blockWitnessBo.setPubKey(minerAddress);
            blockMiners.add(blockWitnessBo);
        }

        //Get the witnessMiners list
        List<BlockWitnessBO> witnessMiners = Lists.newArrayList();
        String witnessAddress = blockHeaderBo.getWitnessAddress();
        if (!StringUtils.isEmpty(witnessAddress)) {
            String[] split = witnessAddress.split(":");
            for (String address : split) {
                BlockWitnessBO blockWitnessBo = new BlockWitnessBO();
                blockWitnessBo.setPubKey(address);
                witnessMiners.add(blockWitnessBo);
            }
        }

        if (CollectionUtils.isNotEmpty(transactionBo)) {
            BlockBO blockBo = new BlockBO();
            BeanUtils.copyProperties(blockHeaderBo, blockBo);
            blockBo.setTransactions(transactionBo);
            blockBo.setBlockMiner(blockMiners);
            blockBo.setBlockWitnesses(witnessMiners);
            blockBo.setNodes(new ArrayList<>());
            return blockBo;
        }
        LOGGER.error("Abnormal results: Querying the block according to the block hash returns an error. blockHash = {}", blockHash);
        return null;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveBlock(BlockHeaderPO blockHeaderPo, Map<String, Object> transaction, List<RewardPO> rewardPos,
                          List<MinerPO> miners, List<String> address, List<UTXOPO> utxoPos) {
        List<TransactionPO> transactionPos = (List<TransactionPO>) transaction.get("bTransactions");
        List<TransactionInputPO> inputs = (List<TransactionInputPO>) transaction.get("inputs");
        List<TransactionOutputPO> outputs = (List<TransactionOutputPO>) transaction.get("outputs");
        List<String> txHashIndexs = (List<String>) transaction.get("txHashIndexs");

        iBlockHeaderService.add(blockHeaderPo);
        iTransactionService.batchInsert(transactionPos);
        iTransactionInputService.batchInsert(inputs);
        iutxoService.batchDeleteUTXO(txHashIndexs);
        iTransactionOutputService.batchInsert(outputs);
        iRewardService.batchInsert(rewardPos);
        iutxoService.batchInsert(utxoPos);
        if (CollectionUtils.isNotEmpty(address)) {
            iMinersService.batchDeleteMiners(address);
        }
        iMinersService.batchSaveOrUpdate(miners);
    }
}
