package cn.primeledger.cas.global.api;

import cn.primeledger.cas.global.api.vo.BlockHeader;
import cn.primeledger.cas.global.api.vo.ResponseData;
import cn.primeledger.cas.global.blockchain.Block;
import cn.primeledger.cas.global.blockchain.BlockService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

import static cn.primeledger.cas.global.constants.RespCodeEnum.*;

/**
 * @author yuanjiantao
 * @date Created on 3/19/2018
 */
@RequestMapping("/version/block")
@RestController
public class BlockApiController {

    @Autowired
    private BlockService blockService;

    @RequestMapping("/info")
    public ResponseData info(String hash) {
        if (null == hash) {
            return new ResponseData(PARAM_INVALID);
        }
        Block block = blockService.getBlock(hash);
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
        Block block = blockService.getBlock(hash);
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

}
