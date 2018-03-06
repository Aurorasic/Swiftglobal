package cn.primeledger.cas.global.consensus;

import cn.primeledger.cas.global.blockchain.Block;
import cn.primeledger.cas.global.utils.ExecutorServices;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Component;

import java.util.concurrent.ExecutorService;

/**
 * @author yuanjiantao
 * @date Created on 3/6/2018
 */
@Component
@Slf4j
public class BlockTimer implements InitializingBean {

    /**
     * 10s interval
     */
    static long INTERVAL = 10 * 1000;

    private Boolean state = false;


    // TODO: 3/6/2018 yuanjiantao how to init the blockTimerData;

    private BlockTimerData blockTimerData;

    private ExecutorService executorService = ExecutorServices.newSingleThreadExecutor("blocktimer", 1000);

    @Override
    public void afterPropertiesSet() throws Exception {
        // TODO: 3/6/2018 yuanjiantao fill the concurrentMap;
    }


    public void start() {
        state = true;
        // TODO: 3/6/2018 yuanjiantao  be ready to mine
    }

    public void time(Block block) {
        if (state) {
            blockTimerData.receiveBlock(block);
            executorService.submit(new BlockTimerTask(block.getHeight() + 1));
        }
    }


    // TODO: 3/6/2018 yuanjiantao whether to receive a block

    public class BlockTimerTask implements Runnable {

        private long height;

        public BlockTimerTask(long height) {
            this.height = height;
        }

        @Override
        public void run() {
            while (true) {
                long preBlockTime = blockTimerData.getTimeByHeight(height - 1);
                if (preBlockTime != -1L && System.currentTimeMillis() - preBlockTime > INTERVAL) {
                    LOGGER.info("no block received in the past 10s");
                    // TODO: 3/6/2018 yuanjiantao post a event

                    break;
                }

            }
        }
    }

}
