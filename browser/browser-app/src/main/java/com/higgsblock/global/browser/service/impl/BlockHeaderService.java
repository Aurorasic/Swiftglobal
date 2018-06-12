package com.higgsblock.global.browser.service.impl;

import com.higgsblock.global.browser.dao.entity.BlockHeaderPO;
import com.higgsblock.global.browser.dao.iface.IBlockHeaderDAO;
import com.higgsblock.global.browser.dao.impl.BlockHeaderDAO;
import com.higgsblock.global.browser.service.bo.BlockHeaderBO;
import com.higgsblock.global.browser.service.bo.PageEntityBO;
import com.higgsblock.global.browser.service.iface.IBlockHeaderService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import static com.higgsblock.global.browser.service.bo.PageEntityBO.createBuilder;

/**
 * @author Su Jiulong
 * @date 2018-05-23
 */
@Service
@Slf4j
public class BlockHeaderService implements IBlockHeaderService {

    @Autowired
    private IBlockHeaderDAO iBlockHeaderDAO;

    @Override
    public long getMaxHeight() {
        return iBlockHeaderDAO.getMaxHeight();
    }

    @Override
    public int add(BlockHeaderPO blockHeaderPo) {
        return iBlockHeaderDAO.add(blockHeaderPo);
    }

    @Override
    public int update(BlockHeaderPO blockHeaderPo) {
        return iBlockHeaderDAO.update(blockHeaderPo);
    }

    @Override
    public int delete(String blockHash) {
        return iBlockHeaderDAO.delete(blockHash);
    }

    @Override
    public BlockHeaderBO getByField(String blockHash) {
        try {
            if (StringUtils.isEmpty(blockHash)) {
                LOGGER.error("blockHash is empty");
                return null;
            }

            List<BlockHeaderPO> blockHeaderPos = iBlockHeaderDAO.getByField(blockHash);
            if (CollectionUtils.isEmpty(blockHeaderPos)) {
                LOGGER.error("The block header that is queried based on the block hash is empty,blockHash = {} ", blockHash);
                return null;
            }

            BlockHeaderPO blockHeaderPo = blockHeaderPos.get(0);
            BlockHeaderBO blockHeaderBo = new BlockHeaderBO();
            BeanUtils.copyProperties(blockHeaderPo, blockHeaderBo);
            blockHeaderBo.setBlockTime(DateFormatUtils.format(blockHeaderPo.getBlockTime(), "yyyy-MM-dd HH:mm:ss"));

            return blockHeaderBo;
        } catch (Exception e) {
            LOGGER.error("get block header by blockHash = {}", e.getMessage());
            return null;
        }
    }

    @Override
    public PageEntityBO<BlockHeaderBO> findScopeBlock(Long start, Long limit) {
        //Get the required data
        List<BlockHeaderPO> blockHeaderPos = iBlockHeaderDAO.getScopeBlock(start, start + limit - 1, IBlockHeaderDAO.ORDER_BY_ASC);
        //Build the results and return
        if (CollectionUtils.isNotEmpty(blockHeaderPos)) {
            List<BlockHeaderBO> blockHeaderBos = getBlockHeaderList(blockHeaderPos);
            return createBuilder().withItmes(blockHeaderBos).withTotal(blockHeaderBos.size()).builder();
        }
        LOGGER.error("Find by scope dataList empty");
        return null;
    }


    @Override
    public List<BlockHeaderBO> getLatestBlock(Long limit) {
        //Get the maximum height in the area block table
        long maxHeight = iBlockHeaderDAO.getMaxHeight();
        if (maxHeight == 0) {
            LOGGER.error("database max height is = {}", maxHeight);
            return null;
        }

        //The beginning of the place
        long start = maxHeight - limit + 1;
        List<BlockHeaderPO> latestBlock = iBlockHeaderDAO.getScopeBlock(start, maxHeight, BlockHeaderDAO.ORDER_BY_DESC);
        if (CollectionUtils.isNotEmpty(latestBlock)) {
            return getBlockHeaderList(latestBlock);
        }

        LOGGER.error("Gets the latest block is empty");
        return null;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void batchInsert(List<BlockHeaderPO> blockHeaderPos) {
        if (CollectionUtils.isEmpty(blockHeaderPos)) {
            LOGGER.warn("blockHeader list is empty");
            return;
        }
        int[] ints = iBlockHeaderDAO.batchInsert(blockHeaderPos);
        for (int anInt : ints) {
            if (anInt < 0 && anInt != Statement.SUCCESS_NO_INFO) {
                LOGGER.error("blockHeader batchInsert result error");
                throw new RuntimeException("blockHeader batchInsert result error");
            }
        }
        LOGGER.info("blockHeader batchInsert success");
    }

    /**
     * BlockHeaderList to BlockHeaderBoList
     *
     * @param headerList
     * @return
     */
    private List<BlockHeaderBO> getBlockHeaderList(List<BlockHeaderPO> headerList) {
        List<BlockHeaderBO> blockHeaderBos = new ArrayList<>();
        headerList.forEach(blockHeader -> {
            BlockHeaderBO blockHeaderBo = new BlockHeaderBO();
            BeanUtils.copyProperties(blockHeader, blockHeaderBo);
            blockHeaderBo.setBlockTime(DateFormatUtils.format(blockHeader.getBlockTime(), "yyyy-MM-dd HH:mm:ss"));
            blockHeaderBos.add(blockHeaderBo);
        });
        return blockHeaderBos;
    }
}
