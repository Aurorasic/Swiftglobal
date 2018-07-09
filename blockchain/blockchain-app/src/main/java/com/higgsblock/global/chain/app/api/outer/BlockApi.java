package com.higgsblock.global.chain.app.api.outer;

import com.google.common.collect.Lists;
import com.higgsblock.global.chain.app.api.vo.BlockHeader;
import com.higgsblock.global.chain.app.api.vo.ResponseData;
import com.higgsblock.global.chain.app.blockchain.Block;
import com.higgsblock.global.chain.app.blockchain.BlockIndex;
import com.higgsblock.global.chain.app.blockchain.BlockService;
import com.higgsblock.global.chain.app.constants.RespCodeEnum;
import com.higgsblock.global.chain.app.service.impl.BlockDaoService;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.higgsblock.global.chain.app.constants.RespCodeEnum.*;

/**
 * @author yuanjiantao
 * @date 3/19/2018
 */
@RequestMapping("/v1.0.0/blocks")
@RestController
public class BlockApi {

    /**
     * The maximum number of single pull blocks
     */
    private static final int MAX_LIMIT = 200;

    @Autowired
    private BlockService blockService;

    @Autowired
    private BlockDaoService blockDaoService;

    /**
     * @param fromHeight
     * @param limit
     * @return
     */
    @RequestMapping("/getBlocks")
    public ResponseData<Map<String, Object>> getBlocksByApiServer(long fromHeight, int limit) {
        if (fromHeight <= 0 || fromHeight >= Long.MAX_VALUE) {
            return new ResponseData<>(RespCodeEnum.PARAM_INVALID, "The height does not exist.");
        }

        if (limit <= 0 || limit > MAX_LIMIT) {
            return new ResponseData<>(RespCodeEnum.PARAM_INVALID, "The maximum number of single " +
                    "pull blocks is " + MAX_LIMIT + "," + " starting from 1");
        }
        //curren max block height
        long currenMaxHeight = blockService.getMaxHeight();
        if (fromHeight > currenMaxHeight) {
            return new ResponseData<>(RespCodeEnum.PARAM_INVALID, "It's already the longest chain height.");
        }

        //The maximum height index of the main chain has been confirmed
        BlockIndex bestMaxBlockIndex = blockService.getLastBestBlockIndex();
        if (bestMaxBlockIndex == null) {
            return new ResponseData<>(RespCodeEnum.HASH_NOT_EXIST, "not found main chain block height and blockHash");
        }

        //The maximum height of the main chain has been confirmed
        long bestMaxHeight = bestMaxBlockIndex.getHeight();
        //best max blockHash
        String bestMaxBlockHash = bestMaxBlockIndex.getBestBlockHash();
        if (bestMaxBlockHash == null) {
            return new ResponseData<>(RespCodeEnum.HASH_NOT_EXIST, "best max block hash is null");
        }
        //return result
        Map<String, Object> result = new HashMap<String, Object>(2);
        //The latest height and hash of the main chain have been confirmed
        Map<String, Object> maxIndex = new HashMap<>(2);
        maxIndex.put("maxHeight", bestMaxHeight);
        maxIndex.put("maxHash", bestMaxBlockHash);
        result.put("maxIndex", maxIndex);

        //Pull block results
        List<Block> resultBlocks = Lists.newArrayList();
        //This variable is equal to fromHeight plus limit
        long resultHeight = fromHeight + limit;
        for (long height = fromHeight; height < resultHeight; height++) {
            List<Block> blocks = blockService.getBlocksByHeight(height);
            if (CollectionUtils.isNotEmpty(blocks)) {
                resultBlocks.addAll(blocks);
            }
        }

        //All blocks that need to be pulled include the fork chain block
        result.put("blocks", resultBlocks);
        return ResponseData.success(result);
    }

    @RequestMapping("/info")
    public ResponseData info(String hash) {
        if (null == hash) {
            return new ResponseData(PARAM_INVALID);
        }
        Block block = blockDaoService.getBlockByHash(hash);
        if (null == block) {
            return new ResponseData(HASH_NOT_EXIST);
        }
        ResponseData<Block> responseData = new ResponseData<>(SUCCESS);
        responseData.setData(block);
        return responseData;
    }

    @RequestMapping("/header")
    public ResponseData header(String hash) {
        if (null == hash) {
            return new ResponseData(PARAM_INVALID);
        }
        Block block = blockDaoService.getBlockByHash(hash);
        if (null == block) {
            return new ResponseData(HASH_NOT_EXIST);
        }
        BlockHeader blockHeader = new BlockHeader();
        BeanUtils.copyProperties(block, blockHeader);
        ResponseData<BlockHeader> responseData = new ResponseData<>(SUCCESS);
        responseData.setData(blockHeader);
        return responseData;
    }

    @RequestMapping("/headerList")
    public ResponseData headerList(long start, long limit) {
        if (start < 1L) {
            start = 1L;
        }
        if (limit < 1) {
            return new ResponseData(PARAM_INVALID);
        }
        long myMaxHeight = blockService.getMaxHeight();
        long maxHeight = start + limit - 1 > myMaxHeight ? myMaxHeight : start + limit - 1;
        List<BlockHeader> list = new ArrayList<>();
        for (long height = start; height < maxHeight + 1; height++) {
            List<Block> temp = blockService.getBlocksByHeight(height);
            temp.forEach(block -> {
                BlockHeader blockHeader = new BlockHeader();
                BeanUtils.copyProperties(block, blockHeader);
                list.add(blockHeader);
            });
        }
        ResponseData<List<BlockHeader>> responseData = new ResponseData<>(SUCCESS);
        responseData.setData(list);
        return responseData;
    }

    @RequestMapping("/recentHeaderList")
    public ResponseData recentHeaderList(long limit) {
        if (limit < 1L) {
            return new ResponseData(PARAM_INVALID);
        }
        long myMaxHeight = blockService.getMaxHeight();
        List<BlockHeader> list = new ArrayList<>();
        for (long height = myMaxHeight; height > myMaxHeight - limit; height--) {
            if (height < 1L) {
                continue;
            }
            List<Block> temp = blockService.getBlocksByHeight(height);
            temp.forEach(block -> {
                BlockHeader blockHeader = new BlockHeader();
                BeanUtils.copyProperties(block, blockHeader);
                list.add(blockHeader);
            });
        }
        ResponseData<List<BlockHeader>> responseData = new ResponseData<>(SUCCESS);
        responseData.setData(list);
        return responseData;
    }

    @RequestMapping("/maxHeight")
    public ResponseData<Long> getMaxHeight() {
        long maxHeight = blockService.getMaxHeight();
        ResponseData<Long> responseData = new ResponseData<Long>(RespCodeEnum.SUCCESS, "success");
        responseData.setData(maxHeight);
        return responseData;
    }

    @RequestMapping("/lastBlock")
    public ResponseData<Block> getBlocksByHeight() {
        ResponseData<Block> responseData = new ResponseData<Block>(RespCodeEnum.SUCCESS, "success");
        return responseData;
    }

    @RequestMapping("/height")
    public ResponseData<List<Block>> getBlocksByHeight(long height) {
        List<Block> blocks = blockService.getBlocksByHeight(height);
        ResponseData<List<Block>> responseData = new ResponseData<List<Block>>(RespCodeEnum.SUCCESS, "success");
        responseData.setData(blocks);
        return responseData;
    }

    @RequestMapping("/buildBlock")
    public ResponseData<Block> buildBlock() {
        Block blocks = blockService.packageNewBlock();
        ResponseData<Block> responseData = new ResponseData<Block>(RespCodeEnum.SUCCESS, "success");
        responseData.setData(blocks);
        return responseData;
    }
}
