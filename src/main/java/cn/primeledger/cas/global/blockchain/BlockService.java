package cn.primeledger.cas.global.blockchain;

import cn.primeledger.cas.global.blockchain.transaction.Transaction;
import cn.primeledger.cas.global.blockchain.transaction.TransactionIndex;
import com.google.common.collect.Lists;
import org.mapdb.HTreeMap;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * @author baizhengwen
 * @date Created in 2018/2/23
 */
@Service
public class BlockService {

    @Resource(name = "blockData")
    private HTreeMap<String, Block> blockMap;

    @Resource(name = "blockIndexData")
    private HTreeMap<String, BlockIndex> blockIndexMap;

    @Resource(name = "transactionIndexData")
    private HTreeMap<String, TransactionIndex> transactionIndexMap;

    /**
     * 创建创世区块
     *
     * @return
     */
    public void genesisBlock() {
        List<Transaction> transactions = Lists.newLinkedList();
        // todo baizhengwen 添加需要写入创世块的交易
        Block block = Block.builder()
                .version((short) 1)
                .blockTime(0)
                .prevBlockHash(null)
                .transactions(transactions)
                .height(1)
                .build();
        persistBlockAndIndex(block);
    }

    public void persistBlockAndIndex(Block block) {
        blockMap.putIfAbsent(block.getHash(), block);

        BlockIndex blockIndex;
        TransactionIndex transactionIndex;

        //build block index
        ArrayList blockHashs = new ArrayList<String>(1);
        if (block.isgenesisBlock()) {
            blockHashs.add(block.getHash());
            blockIndex = new BlockIndex(1, blockHashs, 0);
        } else {
            blockIndex = blockIndexMap.get(block.getHeight());
            BlockIndex preBlockIndex = blockIndexMap.get(block.getHeight() - 1);
            boolean isBest = preBlockIndex == null ? false : preBlockIndex.isBest(block.getPrevBlockHash());

            if (blockIndex == null) {
                blockHashs.add(block.getHash());
                blockIndex = new BlockIndex(1, blockHashs, isBest ? -1 : 0);
            } else {
                blockIndex.getBlockHashs().add(block.getHash());
            }
        }

    }

    public void loadAllBlockData() {
        Iterator<String> blockIterator = blockMap.keySet().iterator();
        while (blockIterator.hasNext()) {
            String next = blockIterator.next();
            Block block = blockMap.get(next);
        }
    }

    public void valid(Block block) {

        // check block struct

        // check signature

        // check transactions

        // persistBlockAndIndex

        // score

        // broadcast
    }

    // for testing
    public void allBlocks() {
//        for (Map.Entry<String, Block> entry : blockMap.entrySet()) {
//            System.out.println(entry.getKey() + ": " + entry.getValue());
//        }
    }
}
