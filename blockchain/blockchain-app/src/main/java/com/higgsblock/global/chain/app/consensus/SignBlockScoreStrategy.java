package com.higgsblock.global.chain.app.consensus;

import com.higgsblock.global.chain.app.blockchain.Block;

/**
 * @author yuguojia
 * @date 2018/03/05
 **/
public class SignBlockScoreStrategy {
    public static int MAX_SIGN_SCORE = 2 << 20;
    public static int MAX_HIGH_GAP = 20;


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