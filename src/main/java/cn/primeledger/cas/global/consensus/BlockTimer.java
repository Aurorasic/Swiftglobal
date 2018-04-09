package cn.primeledger.cas.global.consensus;

import cn.primeledger.cas.global.api.service.UTXORespService;
import cn.primeledger.cas.global.blockchain.Block;
import cn.primeledger.cas.global.blockchain.BlockService;
import cn.primeledger.cas.global.blockchain.transaction.TransactionCacheManager;
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
 * @author yangyi
 * @date Created on 3/6/2018
 */
@Component
@Slf4j
public class BlockTimer implements InitializingBean {

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

    @Autowired
    private KeyPair peerKeyPair;

    @Autowired
    private UTXORespService utxoRespService;

    @Autowired
    private TransactionCacheManager txCacheManager;

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
            long myMaxHeight = blockService.getBestMaxHeight();
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
        if (blockService.hasBestBlock(height) && map.containsKey(height) && map.get(height).compareTo(Boolean.FALSE) == 0) {
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
                    Block bestBlock = blockService.getBestBlockByHeight(block.getHeight());
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
                    long lastHeight = bestBlock.getHeight();
                    if (lastHeight == this.height + 3) {
                        return;
                    }
                    map.put(this.height, true);

                    LOGGER.info("Last bestBlock height: {}", lastHeight);
                }
                if (iscontinue.compareTo(Boolean.TRUE) == 0) {
                    while (true) {
                        String inAddress = ECKey.pubKey2Base58Address("02560157ed444430b566494bf1f22e269b7874f2ca285e38dabf43c9bf41fa24e2");
                        /*String selfAddress = ECKey.pubKey2Base58Address(peerKeyPair.getPubKey());
                        List<UTXO> utxos = utxoRespService.getUTXOsByAddress(selfAddress);
                        Transaction transaction = collectSignService.buildTransaction(utxos, BigDecimal.valueOf(1), inAddress, 0L, (short) 0, null);
                        txCacheManager.addTransaction(transaction);*/
                        try {
                            block = blockService.packageNewBlock();

                            if (block != null) {
                                if (!blockService.validBlockTransactions(block)) {
                                    continue;
                                }
                            }
                        } catch (Exception e) {
                            LOGGER.info(e.getMessage(), e);
                            continue;
                        }
                        if (block == null) {
                            LOGGER.warn("#######packageNewBlock is null");
                            break;
                        }
                        if (block.getHeight() > this.height + CollectSignService.witnessNum) {
                            map.put(this.height, Boolean.FALSE);
                            return;
                        }
                        LOGGER.info("the block height is {},the maxHeight is {}", block.getHeight(), this.height + CollectSignService.witnessNum);
                        block = collectSignService.sendBlockToWitness(block);
                        if (block == null) {
                            LOGGER.warn("#######sendBlockToWitness block is null");
                            break;
                        }
                        map.put(this.height, Boolean.FALSE);
                        LOGGER.info("start sendBlockToWitness! height:{}  pubKey :{} ", block.getHeight(), myaddr);
                        break;
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
