package cn.primeledger.cas.global.consensus.syncblock;

import cn.primeledger.cas.global.blockchain.BlockService;
import cn.primeledger.cas.global.blockchain.listener.MessageCenter;
import cn.primeledger.cas.global.common.listener.IEventBusListener;
import com.google.common.eventbus.Subscribe;
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
