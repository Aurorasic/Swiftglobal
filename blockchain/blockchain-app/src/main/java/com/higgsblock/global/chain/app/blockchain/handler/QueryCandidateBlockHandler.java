package com.higgsblock.global.chain.app.blockchain.handler;

import com.higgsblock.global.chain.app.blockchain.Block;
import com.higgsblock.global.chain.app.blockchain.CandidateBlock;
import com.higgsblock.global.chain.app.blockchain.QueryCandidateBlocks;
import com.higgsblock.global.chain.app.blockchain.listener.MessageCenter;
import com.higgsblock.global.chain.app.common.SocketRequest;
import com.higgsblock.global.chain.app.common.handler.BaseEntityHandler;
import com.higgsblock.global.chain.app.consensus.sign.service.WitnessService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * @author zhaoxiaogang
 * @date 2018/2/28
 */
@Component("queryCandidateBlockHandler")
@Slf4j
@Deprecated
public class QueryCandidateBlockHandler extends BaseEntityHandler<QueryCandidateBlocks> {

    @Autowired
    private WitnessService witnessService;

    @Autowired
    private MessageCenter messageCenter;

    @Override
    protected void process(SocketRequest<QueryCandidateBlocks> request) {
        QueryCandidateBlocks data = request.getData();
        String sourceId = request.getSourceId();
        if (data != null) {
            List<String> blockHashs = data.getBlockHashs();
            List<Block> blocks = witnessService.getCandidateBlocksByHashs(blockHashs);
            CandidateBlock block = new CandidateBlock();
            block.setBlocks(blocks);
            messageCenter.unicast(sourceId, blockHashs);
        }
    }

}
