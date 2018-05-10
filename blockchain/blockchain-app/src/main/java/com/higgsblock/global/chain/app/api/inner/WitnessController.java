package com.higgsblock.global.chain.app.api.inner;

import com.higgsblock.global.chain.app.blockchain.Block;
import com.higgsblock.global.chain.app.consensus.sign.service.WitnessService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collection;

/**
 * @author yangyi
 * @deta 2018/4/26
 * @description
 */
@RestController
@RequestMapping("/witness")
@Slf4j
public class WitnessController {

    @Autowired
    private WitnessService witnessService;

    @RequestMapping("/sendBlockToWitness")
    public boolean sendBlockToWitness(@RequestBody Block block) {
        return witnessService.addCandidateBlockFromMiner(block);
    }

    @RequestMapping("/getCandidateBlocksByHeight")
    public Collection<Block> getCandidateBlocksByHeight(@RequestBody long height) {
        return witnessService.getCandidateBlocksByHeight(height);
    }

    @RequestMapping("/getRecommendBlock")
    public Block getRecommendBlock(@RequestBody long height) {
        Block recommendBlock = witnessService.getRecommendBlock(height);
        LOGGER.info("select the recommend block success {}", recommendBlock);
        return recommendBlock;
    }

    @RequestMapping("/getCandidateBlockHashs")
    Collection<String> getCandidateBlockHashs(@RequestBody long height) {
        return witnessService.getCandidateBlockHashs(height);
    }

    @RequestMapping("/getCandidateBlocksByHashs")
    Collection<Block> getCandidateBlocksByHashs(@RequestBody Collection<String> blockHashs) {
        Collection<Block> result = witnessService.getCandidateBlocksByHashs(blockHashs);
        LOGGER.info("get request params blockHashs={}, return blocks size={}", blockHashs, result.size());
        return result;
    }

    @RequestMapping("/putBlocksToWitness")
    Boolean putBlocksToWitness(@RequestBody Collection<Block> blocks) {
        if (CollectionUtils.isNotEmpty(blocks)) {
            witnessService.addCandidateBlocksFromWitness(blocks);
        }
        return true;
    }
}
