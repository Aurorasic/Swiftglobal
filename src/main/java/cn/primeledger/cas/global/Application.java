package cn.primeledger.cas.global;

import cn.primeledger.cas.global.blockchain.BlockService;
import cn.primeledger.cas.global.blockchain.transaction.TransactionCacheManager;
import cn.primeledger.cas.global.blockchain.transaction.TransactionService;
import cn.primeledger.cas.global.common.listener.IEventBusListener;
import cn.primeledger.cas.global.config.Network;
import cn.primeledger.cas.global.config.NetworkType;
import cn.primeledger.cas.global.p2p.NetworkMgr;
import cn.primeledger.cas.global.utils.ExecutorServices;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.google.common.eventbus.AsyncEventBus;
import com.google.common.eventbus.EventBus;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.concurrent.BasicThreadFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

import java.util.List;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @author baizhengwen
 * @create 2017-03-07 15:55
 */
@Slf4j
@Configuration
@ComponentScan({"cn.primeledger.cas.global"})
public class Application {

    public static final EventBus EVENT_BUS = new AsyncEventBus(ExecutorServices.newFixedThreadPool(
            "AsyncEventBus", Runtime.getRuntime().availableProcessors() * 2, 10000
    ));

    @Autowired
    private BlockService blockService;
    @Autowired
    private TransactionCacheManager txCacheManager;
    @Autowired
    private TransactionService transactionService;
    @Autowired
    private List<IEventBusListener> eventBusListeners;

    @Autowired
    private NetworkMgr networkMgr;

    public static void main(String[] args) throws Exception {
        // Cyclic reference detection
        JSON.DEFAULT_GENERATE_FEATURE |= SerializerFeature.DisableCircularReferenceDetect.getMask();
        // Include fields with a value of null
        JSON.DEFAULT_GENERATE_FEATURE |= SerializerFeature.WriteMapNullValue.getMask();
        // The first level fields are sorted alphabetically
        JSON.DEFAULT_GENERATE_FEATURE |= SerializerFeature.SortField.getMask();
        // Nested fields are sorted alphabetically
        JSON.DEFAULT_GENERATE_FEATURE |= SerializerFeature.MapSortField.getMask();

        ApplicationContext context = new AnnotationConfigApplicationContext(Application.class);


        Application application = context.getBean(Application.class);
        application.start(context);
    }

    private void start(ApplicationContext context) throws Exception {
        // init genesis block
        blockService.initGenesisBlock();
        printAllDBData();
        // register event listeners
        registerEventListeners();
        test();

        LOGGER.info("started...");

        startNetwork(context);
    }

    private void startNetwork(ApplicationContext context) {
        final Network network = new Network.Builder()
                .networkType(NetworkType.DEVNET)
                .context(context)
                .build();

        networkMgr.setNetwork(network);
        networkMgr.start();
    }

    private void registerEventListeners() {
        CollectionUtils.forAllDo(eventBusListeners, EVENT_BUS::register);
    }

    private void test() {
        ScheduledExecutorService executorService = new ScheduledThreadPoolExecutor(2,
                new BasicThreadFactory.Builder().namingPattern("test-schedule-pool").daemon(true).build());

        //block every 10 seconds
        executorService.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                if (!txCacheManager.hasTx()) {
                    return;
                }
                try {
                    blockService.packageNewBlock();
                } catch (Exception e) {
                    LOGGER.error("packageNewBlock error:" + e.getMessage());
                }
            }
        }, 2, 3, TimeUnit.SECONDS);

//        executorService.scheduleAtFixedRate(new Runnable() {
//            @Override
//            public void run() {
//                String address = "1LYHUfw91EjUtqdnSTYjUh7qS46TNCUZZT";
//                TransferTx transaction = transactionService.buildTransfer(BigDecimal.ONE, address, 0L, (short) 0, null);
//                if (transaction != null) {
//                    txCacheManager.addTransaction(transaction);
//                }
//
//            }
//        }, 2, 1, TimeUnit.SECONDS);
    }

    private void printAllDBData() {
        blockService.printAllBlockData();
    }
}
