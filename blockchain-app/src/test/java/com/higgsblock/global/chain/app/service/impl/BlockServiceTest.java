package com.higgsblock.global.chain.app.service.impl;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.google.common.eventbus.EventBus;
import com.higgsblock.global.chain.app.BaseMockTest;
import com.higgsblock.global.chain.app.blockchain.*;
import com.higgsblock.global.chain.app.blockchain.exception.BlockInvalidException;
import com.higgsblock.global.chain.app.blockchain.transaction.SortResult;
import com.higgsblock.global.chain.app.blockchain.transaction.Transaction;
import com.higgsblock.global.chain.app.blockchain.transaction.TransactionCacheManager;
import com.higgsblock.global.chain.app.common.SystemStatusManager;
import com.higgsblock.global.chain.app.config.AppConfig;
import com.higgsblock.global.chain.app.dao.IBlockRepository;
import com.higgsblock.global.chain.app.dao.entity.BlockEntity;
import com.higgsblock.global.chain.app.net.peer.PeerManager;
import com.higgsblock.global.chain.app.service.*;
import com.higgsblock.global.chain.crypto.ECKey;
import com.higgsblock.global.chain.crypto.KeyPair;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.api.mockito.internal.mockcreation.RuntimeExceptionProxy;
import org.powermock.core.classloader.annotations.PrepareForTest;

import java.util.*;

import static org.mockito.ArgumentMatchers.*;

/**
 * @author yangshenghong
 * @date 2018-09-26
 */
@PrepareForTest(value = {BlockService.class, ECKey.class})
public class BlockServiceTest extends BaseMockTest {
    @InjectMocks
    private BlockService blockService;

    @InjectMocks
    @Spy
    private BlockService spyBlockService = new BlockService();

    @Mock
    private AppConfig config;
    @Mock
    private EventBus eventBus;
    @Mock
    private IBlockRepository blockRepository;
    @Mock
    private OrphanBlockCacheManager orphanBlockCacheManager;
    @Mock
    private IBlockIndexService blockIndexService;
    @Mock
    private TransactionCacheManager txCacheManager;
    @Mock
    private UTXOServiceProxy utxoServiceProxy;
    @Mock
    private IScoreService scoreService;
    @Mock
    private PeerManager peerManager;
    @Mock
    private IDposService dposService;
    @Mock
    private IWitnessService witnessService;
    @Mock
    private KeyPair peerKeyPair;
    @Mock
    private ITransactionIndexService transactionIndexService;
    @Mock
    private ITransactionFeeService transactionFeeService;
    @Mock
    private SystemStatusManager systemStatusManager;
    @Mock
    private IBlockChainService blockChainService;
    @Mock
    private IBlockChainInfoService blockChainInfoService;

    @Test
    public void buildWitnessSignInfo() throws Exception {
        PowerMockito.spy(BlockService.class);
        long height = 2L;
        String blockHash = "blockHash";
        int voteVersion = 1;
        //blockHash is not empty
        String result = "blockHash is not empty";
        PowerMockito.when(BlockService.class, "buildWitnessSignInfo", height, blockHash, voteVersion).thenReturn(result);
        Assert.assertEquals(BlockService.buildWitnessSignInfo(height, blockHash, voteVersion), result);

        //blockHash is empty
        result = "blockHash is empty";
        PowerMockito.when(BlockService.class, "buildWitnessSignInfo", height, blockHash, voteVersion).thenReturn(result);
        Assert.assertEquals(BlockService.buildWitnessSignInfo(height, blockHash, voteVersion), result);
    }

    @Test
    public void validSign() throws Exception {
        PowerMockito.spy(BlockService.class);
        PowerMockito.mockStatic(ECKey.class);
        long height = 2L;
        String blockHash = "blockHash";
        int voteVersion = 1;
        String sign = "sign";
        String pubKey = "pubKey";
        String result = "message";
        //ECKey verifySign true
        PowerMockito.when(BlockService.class, "buildWitnessSignInfo", height, blockHash, voteVersion).thenReturn(result);
        PowerMockito.when(ECKey.class, "verifySign", result, sign, pubKey).thenReturn(true);
        Assert.assertTrue(BlockService.validSign(height, blockHash, voteVersion, sign, pubKey));

        //ECKey  verifySign false
        PowerMockito.when(BlockService.class, "buildWitnessSignInfo", height, blockHash, voteVersion).thenReturn(result);
        PowerMockito.when(ECKey.class, "verifySign", result, sign, pubKey).thenReturn(false);
        Assert.assertFalse(BlockService.validSign(height, blockHash, voteVersion, sign, pubKey));

    }

    @Test
    public void getBlockByHash() throws Exception {
        String blockHash = "blockHash";
        //find block by block hash is null
        PowerMockito.when(blockRepository.findByBlockHash(blockHash)).thenReturn(null);
        Assert.assertNull(blockService.getBlockByHash(blockHash));

        //The results are queried according to the hash
        BlockEntity blockEntity = new BlockEntity();
        Block block = new Block();
        PowerMockito.when(blockRepository.findByBlockHash(blockHash)).thenReturn(blockEntity);
        PowerMockito.when(spyBlockService, "covertToBlock", blockEntity).thenReturn(block);
        Assert.assertEquals(spyBlockService.getBlockByHash(blockHash), block);
    }

    @Test
    public void getBlocksByHeight() throws Exception {
        long height = 2L;
        //According to height query result is empty
        PowerMockito.when(blockRepository.findByHeight(height)).thenReturn(Collections.emptyList());
        Assert.assertNotNull(blockService.getBlocksByHeight(height));

        //According to height query result is null
        PowerMockito.when(blockRepository.findByHeight(height)).thenReturn(null);
        Assert.assertNotNull(blockService.getBlocksByHeight(height));

        //According to height query result is not empty
        List<BlockEntity> blockEntities = new LinkedList<>();
        BlockEntity blockEntity = new BlockEntity();
        blockEntities.add(blockEntity);
        Block block = new Block();
        PowerMockito.when(blockRepository.findByHeight(height)).thenReturn(blockEntities);
        PowerMockito.when(spyBlockService, "covertToBlock", blockEntity).thenReturn(block);
        Assert.assertEquals(spyBlockService.getBlocksByHeight(height).get(0), block);
    }

    @Test
    public void getBestBlockByHeight() throws Exception {
        long height = 2L;
        //get blockIndex by height is null
        PowerMockito.when(blockIndexService.getBlockIndexByHeight(height)).thenReturn(null);
        Assert.assertNull(blockService.getBestBlockByHeight(height));
        //
        ////bestBlockHash is null
        BlockIndex blockIndex = new BlockIndex();
        PowerMockito.when(blockIndexService.getBlockIndexByHeight(height)).thenReturn(blockIndex);
        Assert.assertNull(blockService.getBestBlockByHeight(height));

        //get block by bestBlockHash
        String bestBlockHash = "bestBlockHash";
        blockIndex.setBestBlockHash(bestBlockHash);
        Block block = new Block();
        PowerMockito.when(blockIndexService.getBlockIndexByHeight(height)).thenReturn(blockIndex);
        Mockito.doReturn(block).when(spyBlockService).getBlockByHash(bestBlockHash);
        Assert.assertEquals(spyBlockService.getBestBlockByHeight(height), block);
    }

    @Test
    public void saveBlock() throws Exception {
        BlockEntity blockEntity = new BlockEntity();
        Block block = new Block();
        PowerMockito.when(spyBlockService, "convertToBlockEntity", block).thenReturn(blockEntity);
        PowerMockito.when(blockRepository.save(blockEntity)).thenReturn(blockEntity);
        spyBlockService.saveBlock(block);
    }

    @Test
    public void deleteByHeight() {
        long height = 2L;
        int result = 0;
        //There is no corresponding record deletion
        PowerMockito.when(blockRepository.deleteByHeight(height)).thenReturn(result);
        Assert.assertEquals(blockService.deleteByHeight(height), result);

        //fail to delete
        result = -1;
        PowerMockito.when(blockRepository.deleteByHeight(height)).thenReturn(result);
        Assert.assertEquals(blockService.deleteByHeight(height), result);

        //success delete
        result = 1;
        PowerMockito.when(blockRepository.deleteByHeight(height)).thenReturn(result);
        Assert.assertEquals(blockService.deleteByHeight(height), result);
    }

    @Test
    public void checkBlockNumbers() {
        Assert.assertTrue(blockService.checkBlockNumbers());
    }

    @Test
    public void isFirstBlockByHeight() {
        Block block = new Block();
        block.setHeight(1L);
        //When the height is 1, it is directly determined to be the first block
        Assert.assertTrue(blockService.isFirstBlockByHeight(block));

        //When the height is not 1
        block.setHeight(2L);
        //getBlockIndexByHeight is null
        PowerMockito.when(blockIndexService.getBlockIndexByHeight(block.getHeight())).thenReturn(null);
        Assert.assertTrue(blockService.isFirstBlockByHeight(block));

        BlockIndex blockIndex = new BlockIndex();
        PowerMockito.when(blockIndexService.getBlockIndexByHeight(block.getHeight())).thenReturn(blockIndex);
        Assert.assertFalse(blockService.isFirstBlockByHeight(block));
    }

    @Test
    public void getToBeBestBlock() {
        Block block = new Block();
        //genesis block
        block.setHeight(1L);
        Assert.assertEquals(blockService.getToBeBestBlock(block), block);

        //Current height is insufficient to determine the main chain
        block.setHeight(2L);
        Assert.assertNull(blockService.getToBeBestBlock(block));

        block.setHeight(5L);
        //getBlockByHash return null
        PowerMockito.when(spyBlockService.getBlockByHash(block.getPrevBlockHash())).thenReturn(null);
        try {
            blockService.getToBeBestBlock(block);
        } catch (Exception e) {
            Assert.assertTrue(e instanceof IllegalStateException);
            Assert.assertTrue(e.getMessage().contains("can not find block,blockhash"));
            //LOGGER.error(e.toString());
        }

        String preBlockHash = "preBlockHash";
        Block preBlock = new Block();
        preBlock.setHash(preBlockHash);
        BlockIndex blockIndex = new BlockIndex();
        blockIndex.setBestBlockHash(preBlockHash);

        //The last block is the confirmed main chain
        PowerMockito.when(spyBlockService.getBlockByHash(block.getPrevBlockHash())).thenReturn(preBlock);
        PowerMockito.when(blockIndexService.getBlockIndexByHeight(preBlock.getHeight())).thenReturn(blockIndex);
        Assert.assertNull(spyBlockService.getToBeBestBlock(block));

        //block has ready be bestChain
        blockIndex.setBestBlockHash("preBlockHash2");
        block.setPrevBlockHash("preBlockHash2");
        preBlock.setPrevBlockHash("preBlockHash3");
        Mockito.doReturn(preBlock).when(spyBlockService).getBlockByHash(block.getPrevBlockHash());

        //get preBestBlock return null
        Mockito.doReturn(preBlock).doReturn(preBlock).doReturn(null).when(spyBlockService).getBlockByHash(preBlock.getPrevBlockHash());
        try {
            spyBlockService.getToBeBestBlock(block);
        } catch (Exception e) {
            Assert.assertTrue(e instanceof RuntimeExceptionProxy);
            Assert.assertTrue(e.getMessage().contains("block not found"));
        }

        //check preBestBlock failure
        Block preBestBlock = new Block();
        Mockito.doReturn(preBlock).doReturn(preBlock).doReturn(preBestBlock).when(spyBlockService).getBlockByHash(preBlock.getPrevBlockHash());
        PowerMockito.when(spyBlockService.getBestBlockByHeight(preBestBlock.getHeight())).thenReturn(null);
        try {
            spyBlockService.getToBeBestBlock(block);
        } catch (Exception e) {
            Assert.assertTrue(e instanceof RuntimeExceptionProxy);
            Assert.assertTrue(e.getMessage().contains("have not been confirmed best chain"));
        }

        Block bestBlockOfHeight = new Block();
        bestBlockOfHeight.setHash("bestBlockHash1");
        preBestBlock.setHash("bestBlockHash2");
        PowerMockito.when(spyBlockService.getBestBlockByHeight(preBestBlock.getHeight())).thenReturn(bestBlockOfHeight);
        try {
            spyBlockService.getToBeBestBlock(block);
        } catch (Exception e) {
            Assert.assertTrue(e instanceof RuntimeExceptionProxy);
            Assert.assertTrue(e.getMessage().contains("is not match"));
        }

        //getToBeBestBlock
        preBestBlock.setHash("bestBlockHash1");
        Assert.assertEquals(spyBlockService.getToBeBestBlock(block).getHash(), preBlock.getHash());
    }

    @Test
    public void getLastBestBlockIndex() {
        //maxHeight is last best BlockIndex
        BlockIndex maxBlockIndex = new BlockIndex();
        String bestBlockHash = "bestBlockHash";
        maxBlockIndex.setBestBlockHash(bestBlockHash);
        PowerMockito.when(blockIndexService.getLastBlockIndex()).thenReturn(maxBlockIndex);
        Assert.assertEquals(blockService.getLastBestBlockIndex(), maxBlockIndex);

        //maxHeight-- get last best BlockIndex
        maxBlockIndex.setBestBlockHash(null);
        maxBlockIndex.setHeight(3L);
        BlockIndex blockIndex = new BlockIndex();
        blockIndex.setBestBlockHash(bestBlockHash);
        PowerMockito.when(blockIndexService.getBlockIndexByHeight(maxBlockIndex.getHeight() - 1)).thenReturn(blockIndex);
        Assert.assertEquals(blockService.getLastBestBlockIndex(), blockIndex);

        //get GenesisBlock BlockIndex
        blockIndex.setBestBlockHash(null);
        PowerMockito.when(blockIndexService.getBlockIndexByHeight(anyLong())).thenReturn(blockIndex);
        BlockIndex genesisBlockIndex = new BlockIndex();
        PowerMockito.when(blockIndexService.getBlockIndexByHeight(1)).thenReturn(genesisBlockIndex);
        Assert.assertEquals(blockService.getLastBestBlockIndex(), genesisBlockIndex);
    }

    @Test
    public void checkWitnessSignatures() throws Exception {
        //The witness signatures is empty or the signature number is not enough
        Block block = new Block();
        Assert.assertFalse(blockService.checkWitnessSignatures(block));

        //Invalid signature from witness
        List<SignaturePair> witnessSigPKS = new ArrayList<>();
        witnessSigPKS.add(new SignaturePair());
        for (int i = 0; i < 7; i++) {
            witnessSigPKS.add(new SignaturePair("pubKey" + i, "signature" + i));
        }
        block.setWitnessSigPairs(witnessSigPKS);
        Assert.assertFalse(blockService.checkWitnessSignatures(block));

        //Block hash not match signature from witness
        List<SignaturePair> witnessSigPKS2 = new ArrayList<>();
        for (int i = 0; i < 7; i++) {
            witnessSigPKS2.add(new SignaturePair("pubKey" + i, "signature" + i));
        }
        block.setWitnessSigPairs(witnessSigPKS2);
        PowerMockito.mockStatic(BlockService.class);
        PowerMockito.when(BlockService.class, "validSign", anyLong(), anyString(), anyInt(), anyString(), anyString())
                .thenReturn(false);
        Assert.assertFalse(blockService.checkWitnessSignatures(block));

        //The witness is invalid
        PowerMockito.when(BlockService.class, "validSign", anyLong(), anyString(), anyInt(), anyString(), anyString())
                .thenReturn(true);
        PowerMockito.mockStatic(ECKey.class);
        PowerMockito.when(ECKey.class, "pubKey2Base58Address", anyString()).thenReturn("tempAddress");
        PowerMockito.when(witnessService.isWitness(anyString())).thenReturn(false);
        Assert.assertFalse(blockService.checkWitnessSignatures(block));

        //check pass
        PowerMockito.when(witnessService.isWitness(anyString())).thenReturn(true);
        Assert.assertTrue(blockService.checkWitnessSignatures(block));
    }

    @Test
    public void checkDposProducerPermission() throws Exception {
        //The miner signature is invalid
        Block block = new Block();
        Assert.assertFalse(blockService.checkDposProducerPermission(block));

        //Validate the signature of miner failed
        block.setMinerSigPair(new SignaturePair("minerPubKey", "minerSignature"));
        PowerMockito.mockStatic(ECKey.class);
        PowerMockito.when(ECKey.class, "verifySign", anyString(), anyString(), anyString()).thenReturn(false);
        Assert.assertFalse(blockService.checkDposProducerPermission(block));

        //check the current rotation whether the miner should produce the block is false
        PowerMockito.when(ECKey.class, "verifySign", anyString(), anyString(), anyString()).thenReturn(true);
        PowerMockito.when(dposService.checkProducer(block)).thenReturn(false);
        Assert.assertFalse(blockService.checkDposProducerPermission(block));

        //successfully validate block from producer
        PowerMockito.when(dposService.checkProducer(block)).thenReturn(true);
        Assert.assertTrue(blockService.checkDposProducerPermission(block));
    }

    @Test
    public void packageNewBlock() {
        //packageNewBlockForPreBlockHash return null
        String preBlockHash = "preBlockHash";
        Mockito.doReturn(null).when(spyBlockService).packageNewBlockForPreBlockHash(preBlockHash, peerKeyPair);
        Assert.assertNull(spyBlockService.packageNewBlock(preBlockHash));

        //packageNewBlockForPreBlockHash return block
        Block block = new Block();
        Mockito.doReturn(block).when(spyBlockService).packageNewBlockForPreBlockHash(preBlockHash, peerKeyPair);
        Assert.assertEquals(spyBlockService.packageNewBlock(preBlockHash), block);
    }

    @Test
    public void packageNewBlockForPreBlockHash() throws Exception {
        //getLastBlockIndex return null
        String preBlockHash = "preBlockHash";
        PowerMockito.when(blockIndexService.getLastBlockIndex()).thenReturn(null);
        try {
            blockService.packageNewBlockForPreBlockHash(preBlockHash, peerKeyPair);
        } catch (IllegalStateException e) {
            Assert.assertTrue(e instanceof IllegalStateException);
            Assert.assertTrue(e.getMessage().contains("The best block index can not be null"));
        }

        //There are no enough transactions
        BlockIndex lastBlockIndex = new BlockIndex();
        PowerMockito.when(blockIndexService.getLastBlockIndex()).thenReturn(lastBlockIndex);
        Cache<String, Transaction> transactionMap = Caffeine.newBuilder().maximumSize(1).build();
        PowerMockito.when(txCacheManager.getTransactionMap()).thenReturn(transactionMap);
        List txOfUnSpentUtxos = new ArrayList();
        PowerMockito.when(transactionIndexService.getTxOfUnSpentUtxo(anyString(), anyList())).thenReturn(txOfUnSpentUtxos);
        Assert.assertNull(blockService.packageNewBlockForPreBlockHash(preBlockHash, peerKeyPair));

        //sortResult.isOverrun() is false and lastBlockIndex.getHeight()==0
        SortResult sortResult = new SortResult(false, new HashMap<>());
        PowerMockito.when(transactionFeeService.orderTransaction(preBlockHash, txOfUnSpentUtxos)).thenReturn(sortResult);
        txOfUnSpentUtxos.add(new Transaction());
        txOfUnSpentUtxos.add(new Transaction());
        PowerMockito.mockStatic(ECKey.class);
        String sig = "sig";
        PowerMockito.when(ECKey.class, "signMessage", anyString(), anyString()).thenReturn(sig);
        Assert.assertEquals(blockService.packageNewBlockForPreBlockHash(preBlockHash, peerKeyPair).getHeight(), lastBlockIndex.getHeight() + 1);

        //sortResult.isOverrun() is true
        sortResult.setOverrun(true);
        List<Transaction> canPackageTransactionsOfBlock = Arrays.asList(new Transaction(), new Transaction());
        PowerMockito.when(transactionFeeService.getCanPackageTransactionsOfBlock(txOfUnSpentUtxos)).thenReturn(canPackageTransactionsOfBlock);
        Assert.assertEquals(blockService.packageNewBlockForPreBlockHash(preBlockHash, peerKeyPair).getHeight(), lastBlockIndex.getHeight() + 1);

        //lastBlockIndex.getHeight()==2
        lastBlockIndex.setHeight(2L);
        Transaction coinBaseTx = new Transaction();
        coinBaseTx.setHash("coinBaseTx");
        PowerMockito.when(transactionFeeService.buildCoinBaseTx(anyLong(), anyShort(), anyMap(), anyLong())).thenReturn(coinBaseTx);
        Assert.assertEquals(blockService.packageNewBlockForPreBlockHash(preBlockHash, peerKeyPair).getHeight(), lastBlockIndex.getHeight() + 1);
    }

    @Test
    public void persistBlockAndIndex() {
        Block block = new Block();
        block.setHash("blockHash");
        //Blocks already exist on the chain
        PowerMockito.when(blockChainService.isExistBlock(block.getHash())).thenReturn(true);
        try {
            blockService.persistBlockAndIndex(block);
        } catch (Exception e) {
            Assert.assertTrue(e instanceof BlockInvalidException);
            Assert.assertTrue(e.getMessage().contains("the block is not valid"));
        }

        //orphan block
        block.setPrevBlockHash("prevBlockHash");
        PowerMockito.when(blockChainService.isExistBlock(block.getHash())).thenReturn(false);
        PowerMockito.when(blockChainService.isExistBlock(block.getPrevBlockHash())).thenReturn(false);
        try {
            blockService.persistBlockAndIndex(block);
        } catch (Exception e) {
            System.out.println(e.toString());
            Assert.assertTrue(e instanceof BlockInvalidException);
            Assert.assertTrue(e.getMessage().contains("pre block does not exist"));
        }

        //check witness signatures failure
        PowerMockito.when(blockChainService.isExistBlock(block.getPrevBlockHash())).thenReturn(true);
        PowerMockito.when(blockChainService.checkWitnessSignature(block)).thenReturn(false);
        try {
            blockService.persistBlockAndIndex(block);
        } catch (Exception e) {
            Assert.assertTrue(e instanceof BlockInvalidException);
            Assert.assertTrue(e.getMessage().contains("the block is not valid"));
        }

        //check block producer failure
        PowerMockito.when(blockChainService.checkWitnessSignature(block)).thenReturn(true);
        PowerMockito.when(blockChainService.checkBlockProducer(block)).thenReturn(false);
        try {
            blockService.persistBlockAndIndex(block);
        } catch (Exception e) {
            Assert.assertTrue(e instanceof BlockInvalidException);
            Assert.assertTrue(e.getMessage().contains("the block is not valid"));
        }

        //check transaction failure
        PowerMockito.when(blockChainService.checkBlockProducer(block)).thenReturn(true);
        PowerMockito.when(blockChainService.checkTransactions(block)).thenReturn(false);
        try {
            blockService.persistBlockAndIndex(block);
        } catch (Exception e) {
            Assert.assertTrue(e instanceof BlockInvalidException);
            Assert.assertTrue(e.getMessage().contains("the block is not valid"));
        }

        //success
        PowerMockito.when(blockChainService.checkTransactions(block)).thenReturn(true);
        Block newBestBlock = new Block();
        Mockito.doReturn(newBestBlock).when(spyBlockService).saveBlockCompletely(block);
        Assert.assertEquals(spyBlockService.persistBlockAndIndex(block), newBestBlock);
    }

    @Test
    public void doSyncWorksAfterPersistBlock() {
        Block newBestBlock = new Block(),
                persistedBlock = PowerMockito.spy(new Block());
        List<Transaction> transactions = Arrays.asList(new Transaction(), new Transaction());
        persistedBlock.setTransactions(transactions);
        Cache<String, Transaction> transactionMap = Caffeine.newBuilder().maximumSize(1).build();
        Transaction transaction = PowerMockito.spy(new Transaction());
        transactionMap.put("cacheKey", transaction);
        PowerMockito.when(txCacheManager.getTransactionMap()).thenReturn(transactionMap);
        List<String> spendUTXOKeys = Arrays.asList("cacheKey", "cacheKey2");
        PowerMockito.when(persistedBlock.getSpendUTXOKeys()).thenReturn(spendUTXOKeys);

        //The utxo that the current block has spent exists in the cache
        PowerMockito.when(transaction.containsSpendUTXO(anyString())).thenReturn(true);
        blockService.doSyncWorksAfterPersistBlock(newBestBlock, persistedBlock);

        //The utxo that the current block has spent exists not in the cache
        PowerMockito.when(transaction.containsSpendUTXO(anyString())).thenReturn(false);
        blockService.doSyncWorksAfterPersistBlock(newBestBlock, persistedBlock);
    }

    @Test
    public void saveBlockCompletely() {
        Block block = new Block();
        //save block throw exception
        Mockito.doThrow(new RuntimeException()).when(spyBlockService).saveBlock(block);
        try {
            spyBlockService.saveBlockCompletely(block);
        } catch (Exception e) {
            Assert.assertTrue(e instanceof RuntimeException);
        }

        //isFirstBlockByHeight return false
        Mockito.doNothing().when(spyBlockService).saveBlock(block);
        Mockito.doReturn(false).when(spyBlockService).isFirstBlockByHeight(block);
        Mockito.doReturn(null).when(spyBlockService).getToBeBestBlock(block);
        Assert.assertNull(spyBlockService.saveBlockCompletely(block));


        ////isFirstBlockByHeight return true
        Mockito.doReturn(true).when(spyBlockService).isFirstBlockByHeight(block);
        Assert.assertNull(spyBlockService.saveBlockCompletely(block));

        //block.isGenesisBlock()
        block.setHeight(1L);
        Assert.assertNull(spyBlockService.saveBlockCompletely(block));

        //block not GenesisBlock
        block.setHeight(2L);
        Block newBestBlock = new Block();
        Mockito.doReturn(newBestBlock).when(spyBlockService).getToBeBestBlock(block);
        List<String> dpos = new ArrayList<>();
        dpos.add("dpos1");
        PowerMockito.when(dposService.getDposGroupBySn(anyLong())).thenReturn(dpos);
        Assert.assertEquals(spyBlockService.saveBlockCompletely(block), newBestBlock);
    }

    @Test
    public void broadBlockPersistedEvent() {
        Block block = new Block(), newBestBlock = new Block();
        blockService.broadBlockPersistedEvent(block, newBestBlock);
    }

    @Test
    public void loadAllBlockData() throws Exception {
        //checkBlockNumbers fail
        PowerMockito.when(spyBlockService, "checkBlockNumbers").thenReturn(false);
        try {
            spyBlockService.loadAllBlockData();
        } catch (Exception e) {
            Assert.assertTrue(e instanceof RuntimeException);
            Assert.assertTrue(e.getMessage().contains("blockMap size is not equal blockIndexMap count number"));
        }

        //genesis block is incorrect
        PowerMockito.when(spyBlockService, "checkBlockNumbers").thenReturn(true);
        Mockito.doReturn(null).when(spyBlockService).getBlocksByHeight(1);
        try {
            spyBlockService.loadAllBlockData();
        } catch (RuntimeException e) {
            Assert.assertTrue(e instanceof RuntimeException);
            Assert.assertTrue(e.getMessage().contains("genesis block is incorrect"));
        }

        //loadAllBlockData success
        List<Block> blocks = new ArrayList<>(1);
        Block block = new Block();
        block.setHash("GenesisBlockHash");
        block.setHeight(1L);
        blocks.add(block);
        Mockito.doReturn(blocks).when(spyBlockService).getBlocksByHeight(1);
        PowerMockito.when(config.getGenesisBlockHash()).thenReturn("GenesisBlockHash");
        spyBlockService.loadAllBlockData();
    }
}