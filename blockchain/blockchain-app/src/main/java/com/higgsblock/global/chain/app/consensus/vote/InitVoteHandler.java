package com.higgsblock.global.chain.app.consensus.vote;

import com.higgsblock.global.chain.app.common.SocketRequest;
import com.higgsblock.global.chain.app.common.handler.BaseEntityHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * @author yuanjiantao
 * @date 6/29/2018
 */
@Component("initVoteHandler")
@Slf4j
public class InitVoteHandler extends BaseEntityHandler<InitVote> {
    @Override
    protected void process(SocketRequest<InitVote> request) {

    }
}
