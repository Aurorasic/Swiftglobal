package com.higgsblock.global.chain.app.blockchain.consensus.vote;

import com.alibaba.fastjson.JSON;
import com.higgsblock.global.chain.app.blockchain.Block;
import com.higgsblock.global.chain.app.blockchain.SourceBlockResponse;
import com.higgsblock.global.chain.app.blockchain.consensus.sign.service.VoteProcessor;
import com.higgsblock.global.chain.app.blockchain.listener.MessageCenter;
import com.higgsblock.global.chain.app.common.SocketRequest;
import com.higgsblock.global.chain.app.common.handler.BaseEntityHandler;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashMap;

/**
 * @author yuanjiantao
 * @date 7/2/2018
 */
@Component
@Slf4j
public class SourceBlockRequestHandler extends BaseEntityHandler<SourceBlockRequest> {

    @Autowired
    private MessageCenter messageCenter;

    @Autowired
    private VoteProcessor voteProcessor;

    @Override
    protected void process(SocketRequest<SourceBlockRequest> request) {
        String sourceId = request.getSourceId();
        SourceBlockRequest data = request.getData();
        if (null == data || CollectionUtils.isEmpty(data.getBlockHashs())) {
            return;
        }
        LOGGER.info("received sourceBlockReq from {} with data {}", sourceId, JSON.toJSONString(data));
        data.getBlockHashs().forEach(hash -> {
            Block block = voteProcessor.getBlockCache().get(voteProcessor.getHeight(), k -> new HashMap<>()).get(hash);
            if (null != block) {
                messageCenter.unicast(sourceId, new SourceBlockResponse(block));
            }
        });


    }
}
