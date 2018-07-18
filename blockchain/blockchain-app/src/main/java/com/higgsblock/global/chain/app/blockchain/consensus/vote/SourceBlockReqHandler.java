package com.higgsblock.global.chain.app.blockchain.consensus.vote;

import com.alibaba.fastjson.JSON;
import com.higgsblock.global.chain.app.blockchain.Block;
import com.higgsblock.global.chain.app.blockchain.SourceBlock;
import com.higgsblock.global.chain.app.blockchain.listener.MessageCenter;
import com.higgsblock.global.chain.app.common.SocketRequest;
import com.higgsblock.global.chain.app.common.handler.BaseEntityHandler;
import com.higgsblock.global.chain.app.blockchain.consensus.sign.service.VoteService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashMap;

/**
 * @author yuanjiantao
 * @date 7/2/2018
 */
@Component("sourceBlockReqHandler")
@Slf4j
public class SourceBlockReqHandler extends BaseEntityHandler<SourceBlockReq> {

    @Autowired
    private MessageCenter messageCenter;

    @Autowired
    private VoteService voteService;

    @Override
    protected void process(SocketRequest<SourceBlockReq> request) {
        String sourceId = request.getSourceId();
        SourceBlockReq data = request.getData();
        if (null == data || CollectionUtils.isEmpty(data.getBlockHashs())) {
            return;
        }
        LOGGER.info("received sourceBlockReq from {} with data {}", sourceId, JSON.toJSONString(data));
        data.getBlockHashs().forEach(hash -> {
            Block block = voteService.getBlockCache().get(voteService.getHeight(), k -> new HashMap<>()).get(hash);
            if (null != block) {
                messageCenter.unicast(sourceId, new SourceBlock(block));
            }
        });


    }
}
