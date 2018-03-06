package cn.primeledger.cas.global.consensus;

import cn.primeledger.cas.global.blockchain.Block;
import cn.primeledger.cas.global.blockchain.BlockIndex;
import cn.primeledger.cas.global.blockchain.PubKeyAndSignaturePair;
import cn.primeledger.cas.global.blockchain.transaction.BaseTx;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ConcurrentMap;

/**
 * @author yuguojia
 * @date 2018/03/02
 **/
public class MinerScoreStrategy {
    public static int INIT_SCORE = 1000;

    public static int PLUS_SCORE_PACKAGED_SYS_TX_BEST = 50;
    public static int PLUS_SCORE_SIG_PACKAGED_BEST = 20;

    public static int MINUS_SCORE_PACKAGED_BEST = -200;
    public static int MINUS_SCORE_PACKAGED_ERROR = -600;
    public static int MINUS_SCORE_SIG_PACKAGED_ERROR = -100;
    public static int MINUS_SCORE_SKIPPED_PACKAGED = -400;
    public static int MINUS_SCORE_SIG_SAME_HEIGHT_BLOCK = -1000;

    @Autowired
    private static ScoreManager scoreManager;

    @Resource(name = "blockData")
    private static ConcurrentMap<String, Block> blockMap;

    @Resource(name = "blockIndexData")
    private static ConcurrentMap<Long, BlockIndex> blockIndexMap;

    private static void freshDposScoreMap(Block newBlock) {
        if (newBlock.isHundredFirstBlock()) {
            scoreManager.freshDposScoreMap();
        }
    }

    public static void refreshMinersScore(Block newBlock, boolean isBest) {
        PubKeyAndSignaturePair minerPKSig = newBlock.getMinerPKSig();
        List<PubKeyAndSignaturePair> otherPKSigs = newBlock.getOtherPKSigs();
        if (!isBest) {
            //minus miner score
            plusScore(minerPKSig.getAddress(), MINUS_SCORE_PACKAGED_ERROR);
            //minus signers score
            otherPKSigs.forEach(currentBlockOtherPKSig -> {
                plusScore(currentBlockOtherPKSig.getAddress(), MINUS_SCORE_SIG_PACKAGED_ERROR);
            });

        } else {
            freshDposScoreMap(newBlock);
            //minus miner score
            plusScore(minerPKSig.getAddress(), MINUS_SCORE_PACKAGED_BEST);
            //plus signers score
            otherPKSigs.forEach(otherPKSig -> {
                plusScore(otherPKSig.getAddress(), PLUS_SCORE_SIG_PACKAGED_BEST);
            });

            //plus miner score for system transactions
            List<BaseTx> sysTransactions = newBlock.getSysTransactions();
            int plusScore = PLUS_SCORE_PACKAGED_SYS_TX_BEST * sysTransactions.size();
            plusScore(minerPKSig.getAddress(), plusScore);
        }

        //minus signers score for their same height signatures
        BlockIndex blockIndex = blockIndexMap.get(newBlock.getHeight());
        ArrayList<String> blockHashs = blockIndex.getBlockHashs();
        List<Block> sameHeightOtherBlocks = new LinkedList<>();
        blockHashs.forEach(blockHash -> {
            Block otherBlock = blockMap.get(blockHash);
            if (otherBlock != null &&
                    !StringUtils.equals(otherBlock.getHash(), newBlock.getHash())) {
                sameHeightOtherBlocks.add(otherBlock);
            }
        });
        otherPKSigs.forEach(currentBlockOtherPKSig -> {
            //one signer signed same height blocks
            sameHeightOtherBlocks.forEach(sameHeightOtherBlock -> {
                if (sameHeightOtherBlock.isContainOtherPK(currentBlockOtherPKSig.getPubKey())) {
                    plusScore(currentBlockOtherPKSig.getAddress(), MINUS_SCORE_SIG_SAME_HEIGHT_BLOCK);
                }
            });
        });
    }

    /**
     * when the branch and the best chain is switched, calc score again
     *
     * @param oldBestBlock
     * @param newBestBlock
     */
    public static void changeScore(Block oldBestBlock, Block newBestBlock) {
        // reduce old best block miner score
        PubKeyAndSignaturePair oldBestMinerPKSig = oldBestBlock.getMinerPKSig();
        List<PubKeyAndSignaturePair> oldBestOtherPKSigs = oldBestBlock.getOtherPKSigs();
        plusScore(oldBestMinerPKSig.getAddress(),
                MINUS_SCORE_PACKAGED_BEST * -1 + MINUS_SCORE_PACKAGED_ERROR);
        oldBestOtherPKSigs.forEach(otherPKSig -> {
            plusScore(otherPKSig.getAddress(),
                    PLUS_SCORE_SIG_PACKAGED_BEST * -1 + MINUS_SCORE_SIG_PACKAGED_ERROR);
        });
        //reduce miner score for system transaction
        List<BaseTx> sysTransactions = oldBestBlock.getSysTransactions();
        int plusScore = PLUS_SCORE_PACKAGED_SYS_TX_BEST * sysTransactions.size();
        plusScore(oldBestMinerPKSig.getAddress(), plusScore * -1);

        //add miner new best block score
        PubKeyAndSignaturePair newBestMinerPKSig = newBestBlock.getMinerPKSig();
        List<PubKeyAndSignaturePair> newBestOtherPKSigs = newBestBlock.getOtherPKSigs();
        plusScore(newBestMinerPKSig.getAddress(),
                MINUS_SCORE_PACKAGED_ERROR * -1 + MINUS_SCORE_PACKAGED_BEST);
        //minus signers score
        newBestOtherPKSigs.forEach(pair -> {
            plusScore(pair.getAddress(),
                    MINUS_SCORE_SIG_PACKAGED_ERROR * -1 + PLUS_SCORE_SIG_PACKAGED_BEST);
        });
    }

    private static void plusScore(String address, int plusScore) {
        if (StringUtils.isNotEmpty(address) && plusScore != 0) {
            Integer tmpScore = scoreManager.get(address);
            int score = tmpScore == null ? INIT_SCORE : tmpScore;
            score += plusScore;
            scoreManager.put(address, score);
        }// else it is empty address, do not handle
    }
}