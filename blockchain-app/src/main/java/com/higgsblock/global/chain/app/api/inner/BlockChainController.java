package com.higgsblock.global.chain.app.api.inner;

import com.higgsblock.global.chain.app.service.impl.BlockService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * blockchain controller
 *
 * @author baizhengwen
 * @create 2018-03-17
 */
@RequestMapping("/blockchain")
@RestController
public class BlockChainController {

    @Autowired
    private BlockService blockService;

    @RequestMapping("/blockList")
    public Object blockList(long height) {
        return blockService.getBlocksByHeight(height);
    }

    @RequestMapping("/block")
    public Object block(String hash) {
        return blockService.getBlockByHash(hash);
    }
}
