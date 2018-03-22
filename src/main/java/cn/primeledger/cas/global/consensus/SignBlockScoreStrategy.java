package cn.primeledger.cas.global.consensus;

import cn.primeledger.cas.global.blockchain.Block;
import cn.primeledger.cas.global.consensus.sign.service.CollectSignService;

import java.util.List;

/**
 * @author yuguojia
 * @date 2018/03/05
 **/
public class SignBlockScoreStrategy {
    public static int MAX_SIGN_SCORE = 2 << 20;
    public static int MAX_HIGH_GAP = 20;

    public static boolean isAbsoluteHighestScore(long myBlockHeight, List<Long> witnessBlockHeights) {
        if (witnessBlockHeights.size() > CollectSignService.witnessNum) {
            throw new RuntimeException("error witness block height list.");
        }
        int absoluteMaxScore = 0;
        int start = CollectSignService.witnessNum;
        for (int gap = start; gap < (start << 1); gap++) {
            absoluteMaxScore += getScoreByHighGap(gap);
        }

        int myWitnessSignsScore = 0;
        for (Long height : witnessBlockHeights) {
            int gap = (int) (myBlockHeight - height);
            myWitnessSignsScore += getScoreByHighGap(gap);
        }
        return myWitnessSignsScore >= absoluteMaxScore ? true : false;
    }

    public static int calcSignScore(Block myBlock, Block signerBlock) {
        long blockHeight = myBlock.getHeight();
        long signerBlockHeight = signerBlock.getHeight();
        int highGap = (int) (blockHeight - signerBlockHeight);
        int score = getScoreByHighGap(highGap);
        return score;
    }

    public static int getScoreByHighGap(int highGap) {
        if (highGap <= 0) {
            throw new RuntimeException("witness error, it only can sig more height block");
        }
        if (highGap > MAX_HIGH_GAP) {
            highGap = MAX_HIGH_GAP;
        }
        //2<<-1=0,2<<0=2,2<<1=4,...
        int score = MAX_SIGN_SCORE - (2 << (highGap - 2));
        return score;
    }
}