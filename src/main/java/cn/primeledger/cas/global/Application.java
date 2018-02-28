package cn.primeledger.cas.global;

import cn.primeledger.cas.global.blockchain.BlockService;
import cn.primeledger.cas.global.config.Network;
import cn.primeledger.cas.global.config.NetworkType;
import cn.primeledger.cas.global.p2p.NetworkMgr;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.SerializerFeature;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

/**
 * @author baizhengwen
 * @create 2017-03-07 15:55
 */
@Slf4j
@Configuration
@ComponentScan({"cn.primeledger.cas.global"})
public class Application {

    @Autowired
    private BlockService blockService;

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
        application.start();
    }

    private void start() throws Exception {
        // init genesis block
        blockService.genesisBlock();

        LOGGER.info("started...");

        startNetwork();
    }

    private void startNetwork() {
        final Network network = new Network.Builder()
                .networkType(NetworkType.DEVNET)
                .build();

        NetworkMgr networkMgr = new NetworkMgr(network);
        networkMgr.start();
    }
}
