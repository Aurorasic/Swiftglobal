package cn.primeledger.cas.global;

import cn.primeledger.cas.global.blockchain.BlockService;
import cn.primeledger.cas.global.blockchain.PreMiningService;
import cn.primeledger.cas.global.blockchain.transaction.TransactionCacheManager;
import cn.primeledger.cas.global.blockchain.transaction.TransactionService;
import cn.primeledger.cas.global.common.listener.IEventBusListener;
import cn.primeledger.cas.global.config.AppConfig;
import cn.primeledger.cas.global.consensus.NodeManager;
import cn.primeledger.cas.global.crypto.model.KeyPair;
import cn.primeledger.cas.global.network.socket.server.SocketServer;
import cn.primeledger.cas.global.p2p.NetworkMgr;
import cn.primeledger.cas.global.p2p.PeerClient;
import cn.primeledger.cas.global.p2p.PeerMgr;
import cn.primeledger.cas.global.utils.ExecutorServices;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.google.common.eventbus.AsyncEventBus;
import com.google.common.eventbus.EventBus;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.concurrent.BasicThreadFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.ComponentScan;

import java.util.List;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @author baizhengwen
 * @create 2017-03-07 15:55
 */
@Slf4j
@EnableAutoConfiguration
@ComponentScan({"cn.primeledger.cas.global"})
public class Application {

    public static final EventBus EVENT_BUS = new AsyncEventBus(ExecutorServices.newFixedThreadPool(
            "AsyncEventBus", Runtime.getRuntime().availableProcessors() * 2, 10000
    ));


    public static final int PRE_BLOCK_COUNT = 13;

    @Autowired
    private BlockService blockService;
    @Autowired
    private PreMiningService preMiningService;
    @Autowired
    private TransactionCacheManager txCacheManager;
    @Autowired
    private TransactionService transactionService;
    @Autowired
    private List<IEventBusListener> eventBusListeners;

    @Autowired
    private NodeManager nodeManager;

    @Autowired
    private NetworkMgr networkMgr;

    @Autowired
    private AppConfig appConfig;
    @Autowired
    private KeyPair peerKeyPair;

    @Autowired
    private SocketServer socketServer;

    @Autowired
    private PeerClient socketClient;

    @Autowired
    private PeerMgr peerMgr;

    public static void main(String[] args) throws Exception {
        // Cyclic reference detection
        JSON.DEFAULT_GENERATE_FEATURE |= SerializerFeature.DisableCircularReferenceDetect.getMask();
        // Include fields with a value of null
        JSON.DEFAULT_GENERATE_FEATURE |= SerializerFeature.WriteMapNullValue.getMask();
        // The first level fields are sorted alphabetically
        JSON.DEFAULT_GENERATE_FEATURE |= SerializerFeature.SortField.getMask();
        // Nested fields are sorted alphabetically
        JSON.DEFAULT_GENERATE_FEATURE |= SerializerFeature.MapSortField.getMask();

        ApplicationContext context = SpringApplication.run(Application.class, args);


        Application application = context.getBean(Application.class);
        application.start(context);
    }

    private void start(ApplicationContext context) throws Exception {
        // 获取公网ip
        // 如果支持upnp，则进行端口映射
        // 启动本地socket服务端（连接进来的peer必须在一定时间内上报自己的peer信息，服务端才会保持长连接，否则会在超时后丢弃）
        // 从注册中心获取peer信息
        // 作为客户羊尝试连接其他peer服务端（并在连接建立之后，上报自己的peer信息，收到服务端响应后则保持长连接，否则可能会被丢弃）
        // 询问相邻节点区块高度，并开始同步区块qq
        // 同步区块完成后，加载本节点所有区块，并计算索引等相关数据
        // 开始挖矿



        // init genesis block
        preMiningService.initGenesisBlocks();
        blockService.printAllBlockData();
        // register event listeners
        registerEventListeners();
        LOGGER.info("started...");
        try {
//            blockService.loadAllBlockData();
            socketServer.start();
            peerMgr.doGetSeedPeers();
            startNetwork();
            socketClient.register();
            preMiningService.preMiningBlocks();
        } catch (Throwable e) {
            LOGGER.error(e.getMessage(), e);
        }
    }


    private void startNetwork() {
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
                try {
                    if (!txCacheManager.hasTx()) {
                        return;
                    }
                    blockService.packageNewBlock();
                } catch (Exception e) {
                    LOGGER.error("packageNewBlock error:" + e.getMessage());
                }
            }
        }, 2, 3, TimeUnit.SECONDS);
    }
}
