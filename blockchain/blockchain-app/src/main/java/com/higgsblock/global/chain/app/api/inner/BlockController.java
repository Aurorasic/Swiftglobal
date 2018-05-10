package com.higgsblock.global.chain.app.api.inner;

import com.higgsblock.global.chain.app.api.service.BlockRespService;
import com.higgsblock.global.chain.app.api.vo.ResponseData;
import com.higgsblock.global.chain.app.blockchain.Block;
import com.higgsblock.global.chain.app.blockchain.BlockService;
import com.higgsblock.global.chain.app.constants.RespCodeEnum;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * @author baizhengwen
 * @create 2018-03-17
 */
@RequestMapping("/blocks")
@RestController
public class BlockController {

    @Autowired
    private BlockRespService blockRespService;

    @Autowired
    private BlockService blockService;

    @RequestMapping("/sign/response")
    public void signResponse() {
    }

    @RequestMapping("/maxHeight")
    public ResponseData<Long> getMaxHeight() {
        long maxHeight = blockService.getBestMaxHeight();
        ResponseData<Long> responseData = new ResponseData<Long>(RespCodeEnum.SUCCESS, "success");
        responseData.setData(maxHeight);
        return responseData;
    }

    @RequestMapping("/lastBlock")
    public ResponseData<Block> getBlocksByHeight() {
        Block block = blockService.getLastBestBlock();
        ResponseData<Block> responseData = new ResponseData<Block>(RespCodeEnum.SUCCESS, "success");
        responseData.setData(block);
        return responseData;
    }

    @RequestMapping("/blocks")
    public ResponseData<List<Block>> getBlocksByHeight(long height) {
        List<Block> blocks = blockService.getBlocksByHeight(height);
        ResponseData<List<Block>> responseData = new ResponseData<List<Block>>(RespCodeEnum.SUCCESS, "success");
        responseData.setData(blocks);
        return responseData;
    }

}
