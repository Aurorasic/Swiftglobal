package cn.primeledger.cas.global.test.block;

import cn.primeledger.cas.global.Application;
import cn.primeledger.cas.global.common.listener.IEventBusListener;
import cn.primeledger.cas.global.consensus.sign.service.CollectSignService;
import cn.primeledger.cas.global.test.BaseTest;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;

import java.util.List;

import static cn.primeledger.cas.global.Application.EVENT_BUS;

/**
 * @author yangyi
 * @deta 2018/2/26
 * @description
 */
public class BlockTest extends BaseTest {

    @Autowired
    private CollectSignService collectSignService;

    @Autowired
    private List<IEventBusListener> eventBusListeners;

    @Autowired
    private Application application;

    @Autowired
    private ApplicationContext context;




    @Test
    public void test() {
        CollectionUtils.forAllDo(eventBusListeners, EVENT_BUS::register);
//        application.startNetwork(context);
        collectSignService.sendBlockToWitness();
    }


}
