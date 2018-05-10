package com.higgsblock.global.chain.app.consensus;

import com.higgsblock.global.chain.app.Application;
import com.higgsblock.global.chain.app.blockchain.Block;
import com.higgsblock.global.chain.app.blockchain.BlockService;
import com.higgsblock.global.chain.app.blockchain.BlockWitness;
import com.higgsblock.global.chain.app.blockchain.transaction.Transaction;
import com.higgsblock.global.chain.app.blockchain.transaction.TransactionService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;

/**
 * @author yuguojia
 * @date 2018/03/02
 **/
@Component
@Slf4j
public class MinerScoreStrategy {
    public static int INIT_SCORE = 1000;
    public static int FRESH_SCORE_BLOCK_NUM = 5 * NodeSelector.BATCHBLOCKNUM;

    //    public static int PLUS_SCORE_PACKAGED_SYS_TX_BEST = 50;
//    public static int PLUS_SCORE_SIG_PACKAGED_BEST = 20;

    public static int MINUS_SCORE_PACKAGED_BEST = -200;
    public static int MINUS_SCORE_PACKAGED_ERROR = -600;
//    public static int MINUS_SCORE_SIG_PACKAGED_ERROR = -100;
//    public static int MINUS_SCORE_SKIPPED_PACKAGED_BEST = -400;
//    public static int MINUS_SCORE_SIG_SAME_HEIGHT_BLOCK = -1000;

    private static ScoreManager scoreManager;
    private static BlockService blockService;
    private static TransactionService transactionService;
    private static NodeManager nodeManager;

    private static void freshDposScoreMap(Block newBlock) {
        boolean needToFreshDposScore = newBlock.getHeight() <= Application.PRE_BLOCK_COUNT &&
                newBlock.isDPosEndHeight();

        if (newBlock.isPowerHeight(FRESH_SCORE_BLOCK_NUM) || needToFreshDposScore) {
            scoreManager.freshDposScoreMap();
            LOGGER.info("after freshDposScoreMap, dpos miner score info size:" + scoreManager.getDposMinerSoreMap().size());
        }
    }

    private static void handlePreMiningScoreMap(Block newBlock) {
        if (newBlock.getHeight() <= Application.PRE_BLOCK_COUNT) {
            scoreManager.freshDposScoreMap();
        }
    }

    public static void refreshMinersScore(Block bestBlock) {
        if (bestBlock == null) {
            return;
        }
        BlockWitness minerPKSig = bestBlock.getMinerFirstPKSig();
        List<BlockWitness> otherPKSigs = bestBlock.getOtherWitnessSigPKS();
        //minus miner score
        plusScore(minerPKSig.getAddress(), MINUS_SCORE_PACKAGED_BEST);
        //plus signers score
//        otherPKSigs.forEach(otherPKSig -> {
//            plusScore(otherPKSig.getAddress(), PLUS_SCORE_SIG_PACKAGED_BEST);
//        });

        //handle joined miner and removed miner
        List<Transaction> transactions = bestBlock.getTransactions();
        for (Transaction tx : transactions) {
            Set<String> removedMiners = transactionService.getRemovedMiners(tx);
            for (String removedMiner : removedMiners) {
                scoreManager.remove(removedMiner);
            }
            Set<String> addedMiners = transactionService.getAddedMiners(tx);
            for (String addedMiner : addedMiners) {
                scoreManager.putIfAbsent(addedMiner, INIT_SCORE);
            }
        }
        LOGGER.info("handled joined miner and removed miner,bestBlock={}", bestBlock.getHash());

        //minus signers score for their same height signatures
//        List<Block> sameHeightOtherBlocks = blockService.getBlocksByHeightExclude(bestBlock.getHeight(), bestBlock.getHash());
//        otherPKSigs.forEach(currentBlockOtherPKSig -> {
//            //one signer signed same height blocks
//            sameHeightOtherBlocks.forEach(sameHeightOtherBlock -> {
//                if (sameHeightOtherBlock.isContainOtherPK(currentBlockOtherPKSig.getPubKey())) {
//                    plusScore(currentBlockOtherPKSig.getAddress(), MINUS_SCORE_SIG_SAME_HEIGHT_BLOCK);
//                }
//            });
//        });
        freshDposScoreMap(bestBlock);
    }

    public static void refreshMinersScore(Block newBlock, boolean isBest) {
        BlockWitness minerPKSig = newBlock.getMinerFirstPKSig();
        List<BlockWitness> otherPKSigs = newBlock.getOtherWitnessSigPKS();
        if (!isBest) {
            //minus miner score
//            plusScore(minerPKSig.getAddress(), MINUS_SCORE_PACKAGED_ERROR);
            //minus signers score
//            otherPKSigs.forEach(currentBlockOtherPKSig -> {
//                plusScore(currentBlockOtherPKSig.getAddress(), MINUS_SCORE_SIG_PACKAGED_ERROR);
//            });

        } else {
            //minus miner score
            plusScore(minerPKSig.getAddress(), MINUS_SCORE_PACKAGED_BEST);
            //plus signers score
//            otherPKSigs.forEach(otherPKSig -> {
//                plusScore(otherPKSig.getAddress(), PLUS_SCORE_SIG_PACKAGED_BEST);
//            });

            //handle joined miner and removed miner
            List<Transaction> transactions = newBlock.getTransactions();
            for (Transaction tx : transactions) {
                Set<String> removedMiners = transactionService.getRemovedMiners(tx);
                for (String removedMiner : removedMiners) {
                    scoreManager.remove(removedMiner);
                }
                Set<String> addedMiners = transactionService.getAddedMiners(tx);
                for (String addedMiner : addedMiners) {
                    scoreManager.putIfAbsent(addedMiner, INIT_SCORE);
                }
            }
        }

        //minus signers score for their same height signatures
//        List<Block> sameHeightOtherBlocks = blockService.getBlocksByHeightExclude(newBlock.getHeight(), newBlock.getHash());
//        otherPKSigs.forEach(currentBlockOtherPKSig -> {
//            //one signer signed same height blocks
//            sameHeightOtherBlocks.forEach(sameHeightOtherBlock -> {
//                if (sameHeightOtherBlock.isContainOtherPK(currentBlockOtherPKSig.getPubKey())) {
//                    plusScore(currentBlockOtherPKSig.getAddress(), MINUS_SCORE_SIG_SAME_HEIGHT_BLOCK);
//                }
//            });
//        });
        freshDposScoreMap(newBlock);
    }

    /**
     * when the branch and the best chain is switched, calc score again
     *
     * @param oldBestBlock
     * @param newBestBlock
     */
    public static void changeScore(Block oldBestBlock, Block newBestBlock) {
        // 1.1 rollback :reduce old best block miner score
        BlockWitness oldBestMinerPKSig = oldBestBlock.getMinerFirstPKSig();
        List<BlockWitness> oldBestOtherPKSigs = oldBestBlock.getOtherWitnessSigPKS();
        plusScore(oldBestMinerPKSig.getAddress(),
                MINUS_SCORE_PACKAGED_BEST * -1 + MINUS_SCORE_PACKAGED_ERROR);
//        oldBestOtherPKSigs.forEach(otherPKSig -> {
//            plusScore(otherPKSig.getAddress(),
//                    PLUS_SCORE_SIG_PACKAGED_BEST * -1 + MINUS_SCORE_SIG_PACKAGED_ERROR);
//        });
        //1.2 add miner new best block score
        BlockWitness newBestMinerPKSig = newBestBlock.getMinerFirstPKSig();
        List<BlockWitness> newBestOtherPKSigs = newBestBlock.getOtherWitnessSigPKS();
        plusScore(newBestMinerPKSig.getAddress(),
                MINUS_SCORE_PACKAGED_ERROR * -1 + MINUS_SCORE_PACKAGED_BEST);

        //2.1 rollback :reduce signers score for old best system transaction
//        oldBestOtherPKSigs.forEach(pair -> {
//            plusScore(pair.getAddress(),
//                    PLUS_SCORE_SIG_PACKAGED_BEST * -1 + MINUS_SCORE_SIG_PACKAGED_ERROR);
//        });
        //2.2 plus signers score for new best chain
//        newBestOtherPKSigs.forEach(pair -> {
//            plusScore(pair.getAddress(),
//                    MINUS_SCORE_SIG_PACKAGED_ERROR * -1 + PLUS_SCORE_SIG_PACKAGED_BEST);
//        });

        //3.1 rollback :add miner score for skipped old best blocking
//        handleSkippedMinerScore(oldBestBlock, true);
        //3.2 minus miner score for skipped blocking of new best chain
//        handleSkippedMinerScore(newBestBlock, false);
    }

    private static void plusScore(String address, int plusScore) {
        if (StringUtils.isNotEmpty(address) && plusScore != 0) {
            Integer tmpScore = scoreManager.get(address);
            int score = tmpScore == null ? INIT_SCORE : tmpScore;
            score += plusScore;
            scoreManager.put(address, score);
        }// else it is empty address, do not handle
    }

//    private static void handleSkippedMinerScore(Block block, boolean rollBack) {
//        if (StringUtils.isEmpty(block.getPrevBlockHash())) {
//            return;
//        }
//        List<String> skippedAddressList = nodeManager.getUnPackNode(
//                blockService.getBlock(block.getPrevBlockHash()), block);
//        int ratio = rollBack ? -1 : 1;
//        for (String skippedAddress : skippedAddressList) {
//            plusScore(skippedAddress, MINUS_SCORE_SKIPPED_PACKAGED_BEST * ratio);
//        }
//    }

    @Autowired(required = true)
    public void setScoreManager(ScoreManager scoreManager) {
        MinerScoreStrategy.scoreManager = scoreManager;
    }

    @Autowired(required = true)
    public void setBlockService(BlockService blockService) {
        MinerScoreStrategy.blockService = blockService;
    }

    @Autowired(required = true)
    public void setTransactionService(TransactionService transactionService) {
        MinerScoreStrategy.transactionService = transactionService;
    }

    @Autowired(required = true)
    public void setBlockService(NodeManager nodeManager) {
        MinerScoreStrategy.nodeManager = nodeManager;
    }

}