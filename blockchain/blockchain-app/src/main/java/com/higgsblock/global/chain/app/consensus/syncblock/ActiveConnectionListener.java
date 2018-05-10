package com.higgsblock.global.chain.app.consensus.syncblock;

import com.higgsblock.global.chain.app.blockchain.BlockService;
import com.higgsblock.global.chain.app.blockchain.listener.MessageCenter;
import com.higgsblock.global.chain.common.eventbus.listener.IEventBusListener;
import com.google.common.eventbus.Subscribe;
import com.higgsblock.global.chain.network.socket.event.ActiveConnectionEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @author yuanjiantao
 * @date Created on 3/26/2018
 */
@Slf4j
@Component
public class ActiveConnectionListener implements IEventBusListener {

    @Autowired
    private MessageCenter messageCenter;

    @Autowired
    private BlockService blockService;

    @Subscribe
    public void process(ActiveConnectionEvent event) {
        messageCenter.unicast(event.getConnection().getPeerId(), new MaxHeight(blockService.getBestMaxHeight()));
    }
}
