package cn.primeledger.cas.global.consensus;

import cn.primeledger.cas.global.blockchain.Block;
import cn.primeledger.cas.global.blockchain.BlockService;
import cn.primeledger.cas.global.consensus.sign.service.CollectSignService;
import cn.primeledger.cas.global.crypto.ECKey;
import cn.primeledger.cas.global.crypto.model.KeyPair;
import cn.primeledger.cas.global.utils.ExecutorServices;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutorService;

/**
 * block timer
 * when non block was received in the past few time , call the function to mine a block
 *
 * @author yuanjiantao
 * @date Created on 3/6/2018
 */
@Component
@Slf4j
public class BlockTimer implements InitializingBean {

    /**
     * 10s interval
     */
    private static final long INTERVAL = 2 * 1000;

    /**
     * my addr
     */
    private String myaddr;


    private volatile ConcurrentMap<Long, Boolean> map;

    /**
     * executorService
     */
    private ExecutorService executorService;

    @Autowired
    private KeyPair keyPair;

    @Autowired
    private CollectSignService collectSignService;

    @Autowired
    private BlockService blockService;


    @Override
    public void afterPropertiesSet() {
        this.executorService = ExecutorServices.newFixedThreadPool("blockTimer", 3, 100);
        this.myaddr = ECKey.pubKey2Base58Address(keyPair);
        this.map = new ConcurrentHashMap<>(5);
    }

    /**
     * @param height
     * @param candidates
     */
    public void processCandidates(long height, List<String> candidates) {

        LOGGER.info("candidates : {}", candidates);

        if (candidates.contains(myaddr)) {
            long myMaxHeight = blockService.getMaxHeight();
            if (myMaxHeight < height - 1) {
                map.putIfAbsent(height, Boolean.FALSE);
            } else {
                map.putIfAbsent(height, Boolean.TRUE);
            }
            executorService.execute(new BlockTimerTask(height));
        }
    }

    public void removeKey(long height) {
        map.remove(height - 4);
    }

    public void processBlock(Block block) {
        long height = block.getHeight();
        if (map.containsKey(height) && map.get(height).compareTo(Boolean.FALSE) == 0) {
            LOGGER.info("change map value to true which key is {},then begin to pack block", height);
            map.put(height, Boolean.TRUE);
        }
    }


    public class BlockTimerTask implements Runnable {

        private long height;

        private Block block;

        public BlockTimerTask(long height) {
            this.height = height;
        }

        @Override
        public void run() {
            while (true) {
                Boolean iscontinue = map.get(height);
                if (null == iscontinue) {
                    break;
                }
                if (block != null) {
                    long height = block.getHeight();
                    Block bestBlock = blockService.getBestBlockByHeight(height);
                    if (bestBlock == null) {
                        try {
                            Thread.sleep(300);
                        } catch (InterruptedException e) {
                            LOGGER.error(e.getMessage(), e);
                        }
                        continue;
                    }
                    String bestBlockHash = bestBlock.getHash();
                    String hash = block.getHash();
                    if (StringUtils.equals(bestBlockHash, hash)) {
                        return;
                    }
                    map.put(this.height, true);
                }
                if (iscontinue.compareTo(Boolean.TRUE) == 0) {
                    while (true) {
                        block = collectSignService.sendBlockToWitness();
                        if (block == null) {
                            break;
                        }
                        map.put(height, Boolean.FALSE);
                        LOGGER.info("start sendBlockToWitness! height:{}  pubKey :{} ", height, myaddr);
                    }
                }
                try {
                    Thread.sleep(300);
                } catch (InterruptedException e) {
                    LOGGER.error(e.getMessage(), e);
                }
            }
        }
    }
}
