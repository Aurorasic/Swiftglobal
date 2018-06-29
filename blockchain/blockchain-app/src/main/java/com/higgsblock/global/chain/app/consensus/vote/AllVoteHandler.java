package com.higgsblock.global.chain.app.consensus.vote;

import com.higgsblock.global.chain.app.common.SocketRequest;
import com.higgsblock.global.chain.app.common.handler.BaseEntityHandler;
import com.higgsblock.global.chain.app.consensus.sign.service.WitnessService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @author yuanjiantao
 * @date 6/29/2018
 */
@Component("allVoteHandler")
@Slf4j
public class AllVoteHandler extends BaseEntityHandler<AllVote> {

    @Autowired
    private WitnessService witnessService;

    @Override
    protected void process(SocketRequest<AllVote> request) {

    }
}
