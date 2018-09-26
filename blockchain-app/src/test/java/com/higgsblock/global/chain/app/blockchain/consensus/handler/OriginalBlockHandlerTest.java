package com.higgsblock.global.chain.app.blockchain.consensus.handler;

import com.google.common.eventbus.EventBus;
import com.higgsblock.global.chain.app.BaseMockTest;
import com.higgsblock.global.chain.app.blockchain.Block;
import com.higgsblock.global.chain.app.blockchain.IBlockChainService;
import com.higgsblock.global.chain.app.blockchain.consensus.message.OriginalBlock;
import com.higgsblock.global.chain.app.blockchain.listener.MessageCenter;
import com.higgsblock.global.chain.app.blockchain.transaction.Transaction;
import com.higgsblock.global.chain.app.net.message.BizMessage;
import com.higgsblock.global.chain.app.service.IVoteService;
import com.higgsblock.global.chain.app.service.IWitnessService;
import com.higgsblock.global.chain.crypto.ECKey;
import com.higgsblock.global.chain.crypto.KeyPair;
import com.higgsblock.global.chain.network.socket.message.IMessage;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.reflect.Whitebox;

import java.util.ArrayList;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;

/**
 * @author Su Jiulong
 * @date 2018/9/26
 */
public class OriginalBlockHandlerTest extends BaseMockTest {

    @InjectMocks
    private OriginalBlockHandler originalBlockHandler;

    @Mock
    private IWitnessService witnessService;

    @Mock
    private MessageCenter messageCenter;

    @Mock
    private IVoteService voteService;

    @Mock
    private IBlockChainService blockChainService;

    @Mock
    private EventBus eventBus;

    @Test
    public void valid() {

        //OriginalBlock is null
        IMessage<OriginalBlock> message = new BizMessage<OriginalBlock>();
        Assert.assertFalse(originalBlockHandler.valid(message));

        //OriginalBlock is not null && block is null
        OriginalBlock originalBlock = new OriginalBlock();
        message = new BizMessage<OriginalBlock>(null, originalBlock);
        Assert.assertFalse(originalBlockHandler.valid(message));

        //OriginalBlock is not null && block is not null && block not valid
        Block block = new Block();
        originalBlock.setBlock(block);
        Assert.assertFalse(originalBlockHandler.valid(message));
        //block valid success
        Block mockBlock = PowerMockito.mock(Block.class);
        originalBlock.setBlock(mockBlock);
        PowerMockito.when(mockBlock.valid()).thenReturn(true);
        Assert.assertTrue(originalBlockHandler.valid(message));
    }

    @Test
    public void process() {
        IMessage<OriginalBlock> message = new BizMessage<OriginalBlock>();
        KeyPair keyPair = new KeyPair();
        ECKey ecKey = new ECKey();
        keyPair.setPubKey(ecKey.getKeyPair().getPubKey());
        Whitebox.setInternalState(originalBlockHandler, "keyPair", keyPair);
        //is not witness node
        PowerMockito.when(witnessService.isWitness(anyString())).thenReturn(false);
        PowerMockito.when(messageCenter.dispatchToWitnesses(any())).thenReturn(true);
        originalBlockHandler.process(message);

        Block block = new Block();
        List<Transaction> transactions = new ArrayList<>(2);
        Transaction transaction1 = new Transaction();
        transactions.add(transaction1);
        //is witness node  and the transaction list size is equal to 1
        block.setTransactions(transactions);
        OriginalBlock originalBlock = new OriginalBlock(block);
        message = new BizMessage<>("sourceId", originalBlock);
        PowerMockito.when(witnessService.isWitness(anyString())).thenReturn(true);
        originalBlockHandler.process(message);

        //the transaction list size is equal to 2
        Transaction transaction2 = new Transaction();
        transactions.add(transaction2);
        long height = 100L;
        String blockHash = "This hash belongs to block 100";
        block.setHeight(height);
        block.setHash(blockHash);
        //This block has voted
        PowerMockito.when(voteService.isExist(height, blockHash)).thenReturn(true);
        originalBlockHandler.process(message);

        //This block has not voted
        PowerMockito.when(voteService.isExist(height, blockHash)).thenReturn(false);
        //the block is already on the chain
        PowerMockito.when(blockChainService.isExistBlock(blockHash)).thenReturn(true);
        originalBlockHandler.process(message);

        //the blockHash is not on the chain
        PowerMockito.when(blockChainService.isExistBlock(blockHash)).thenReturn(false);
        //The block height is less than or equal to the current maximum height
        PowerMockito.when(blockChainService.getMaxHeight()).thenReturn(101L);
        originalBlockHandler.process(message);

        //This block is higher than the current maximum height
        PowerMockito.when(blockChainService.getMaxHeight()).thenReturn(99L);
        String preBlockHash = "preBlockHash";
        block.setPrevBlockHash(preBlockHash);
        //the prev block is not on the chain
        PowerMockito.when(blockChainService.isExistBlock(preBlockHash)).thenReturn(false);
        PowerMockito.doNothing().when(eventBus).post(any());
        PowerMockito.doNothing().when(voteService).addOriginalBlockToCache(block);
        originalBlockHandler.process(message);

        //the prev block is on the chain
        PowerMockito.when(blockChainService.isExistBlock(preBlockHash)).thenReturn(true);
        PowerMockito.doNothing().when(voteService).addOriginalBlock(block);
        PowerMockito.when(messageCenter.dispatchToWitnesses(originalBlock)).thenReturn(true);
        originalBlockHandler.process(message);
    }
}