package cn.primeledger.cas.global.blockchain.listener;

import cn.primeledger.cas.global.blockchain.EntityProcessor;
import cn.primeledger.cas.global.common.event.ReceivedDataEvent;
import cn.primeledger.cas.global.common.listener.IEventBusListener;
import com.alibaba.fastjson.JSON;
import com.google.common.eventbus.Subscribe;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @author baizhengwen
 * @date 2018/2/28
 */
@Component
@Slf4j
public class BlockChainListener implements IEventBusListener {

    @Autowired
    private EntityProcessor processor;

    @Subscribe
    public void process(ReceivedDataEvent event) {
        LOGGER.info("receive new ReceivedDataEvent {}", JSON.toJSONString(event));
        processor.add(event.getEntity());
    }

}
