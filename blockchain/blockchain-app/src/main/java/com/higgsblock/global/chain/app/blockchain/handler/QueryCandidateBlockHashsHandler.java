package com.higgsblock.global.chain.app.blockchain.handler;

import com.higgsblock.global.chain.app.blockchain.CandidateBlockHashs;
import com.higgsblock.global.chain.app.blockchain.QueryCandidateBlockHashs;
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
@Component("queryCandidateBlockHashsHandler")
@Slf4j
@Deprecated
public class QueryCandidateBlockHashsHandler extends BaseEntityHandler<QueryCandidateBlockHashs> {

    @Autowired
    private WitnessService witnessService;

    @Autowired
    private MessageCenter messageCenter;

    @Override
    protected void process(SocketRequest<QueryCandidateBlockHashs> request) {
        QueryCandidateBlockHashs data = request.getData();
        String sourceId = request.getSourceId();
        if (data != null) {
            long height = data.getHeight();
            List<String> candidateBlockHashs = witnessService.getCandidateBlockHashs(height);
            CandidateBlockHashs blockHashs = new CandidateBlockHashs();
            blockHashs.setBlockHashs(candidateBlockHashs);
            messageCenter.unicast(sourceId, blockHashs);
        }
    }

}
