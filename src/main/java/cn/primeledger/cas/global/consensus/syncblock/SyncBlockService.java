package cn.primeledger.cas.global.consensus.syncblock;

import cn.primeledger.cas.global.Application;
import cn.primeledger.cas.global.blockchain.Block;
import cn.primeledger.cas.global.blockchain.BlockService;
import cn.primeledger.cas.global.common.entity.BroadcastMessageEntity;
import cn.primeledger.cas.global.common.entity.UnicastMessageEntity;
import cn.primeledger.cas.global.common.event.BroadcastEvent;
import cn.primeledger.cas.global.common.event.UnicastEvent;
import cn.primeledger.cas.global.crypto.model.KeyPair;
import cn.primeledger.cas.global.p2p.channel.ChannelMgr;
import com.alibaba.fastjson.JSON;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import javafx.util.Pair;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import static cn.primeledger.cas.global.constants.EntityType.*;

/**
 * @author yuanjiantao
 * @date Created on 3/8/2018
 */
@Component
@Slf4j
public class SyncBlockService implements InitializingBean {

    @Autowired
    private BlockService blockService;

    private ThreadPoolExecutor executor;

    private List<Pair<Long, String>> list = Collections.synchronizedList(new ArrayList<>());

    @Autowired
    private ChannelMgr channelMgr;

    @Autowired
    private KeyPair keyPair;

    @Override
    public void afterPropertiesSet() {

        executor = new ThreadPoolExecutor(2, 2, 100L, TimeUnit.MILLISECONDS, new LinkedBlockingDeque<>(),
                new ThreadFactoryBuilder().setNameFormat("sycBlock-pool-%d").build());

        executor.allowCoreThreadTimeOut(true);

        executor.execute(() -> {
            while (true) {
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException e) {
                    LOGGER.error(e.getMessage(), e);
                }
                if (channelMgr.countActionChannels() > 0) {
                    askMaxHeight();
                    break;
                }
            }

            while (true) {
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException e) {
                    LOGGER.error(e.getMessage(), e);
                }
                if (list.size() > 0) {
                    break;
                }
            }

            long syncHeight = blockService.getMaxHeight();
            int startIndex = 0;
            while (syncHeight <= list.get(list.size() - 1).getKey()) {
                if (syncHeight > list.get(startIndex).getKey()) {
                    startIndex++;
                }
                int index = (int) syncHeight % (list.size() - startIndex);
                Inventory inventory = new Inventory();
                inventory.setHeight(syncHeight);
                unicastInventory(inventory, list.get(index + startIndex).getValue());

                if (syncHeight % 10 == 0L || syncHeight == list.get(list.size() - 1).getKey()) {
                    try {
                        Thread.sleep(1500);
                    } catch (InterruptedException e) {
                        LOGGER.error(e.getMessage(), e);
                    }
                }
                syncHeight++;
            }
        });
    }

    public void sendBlock(Block block, String sourceId) {
        UnicastMessageEntity entity = new UnicastMessageEntity();
        entity.setType(BLOCK_BROADCAST.getCode());
        entity.setVersion(block.getVersion());
        entity.setData(JSON.toJSONString(block));
        entity.setSourceId(sourceId);
        Application.EVENT_BUS.post(new UnicastEvent(entity));
        LOGGER.info("send syncblock response");
    }


    public void updateMaxHeight(long height, String sourceId) {
        int size = list.size();
        int i;
        for (i = 0; i < size; i++) {
            if (list.get(i).getKey() > height) {
                list.add(i, new Pair<>(height, sourceId));
                break;
            }
        }
        if (i == size) {
            list.add(new Pair<>(height, sourceId));
        }
    }

    public void askMaxHeight() {
        MaxHeight maxHeight = new MaxHeight(keyPair.getPubKey());
        BroadcastMessageEntity entity = new BroadcastMessageEntity();
        entity.setType(MAXHEIGHT.getCode());
        entity.setVersion(maxHeight.getVersion());
        entity.setData(JSON.toJSONString(maxHeight));
        Application.EVENT_BUS.post(new BroadcastEvent(entity));
    }

    public void unicastMaxHeight(MaxHeight maxHeight, String sourceId) {
        if (null == maxHeight) {
            return;
        }
        UnicastMessageEntity entity = new UnicastMessageEntity();
        entity.setType(MAXHEIGHT.getCode());
        entity.setVersion(maxHeight.getVersion());
        entity.setData(JSON.toJSONString(maxHeight));
        entity.setSourceId(sourceId);
        Application.EVENT_BUS.post(new UnicastEvent(entity));

        LOGGER.info("unicast syncblock success: " + JSON.toJSONString(maxHeight));
    }

    public void unicastInventory(Inventory inventory, String sourceId) {
        if (null == inventory) {
            return;
        }
        UnicastMessageEntity entity = new UnicastMessageEntity();
        entity.setType(INVENTORY.getCode());
        entity.setVersion(inventory.getVersion());
        entity.setData(JSON.toJSONString(inventory));
        entity.setSourceId(sourceId);
        Application.EVENT_BUS.post(new UnicastEvent(entity));

        LOGGER.info("unicast syncBlock message success: " + JSON.toJSONString(inventory));
    }
}
