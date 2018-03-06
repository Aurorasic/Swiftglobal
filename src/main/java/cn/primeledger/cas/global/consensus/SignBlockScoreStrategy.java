package cn.primeledger.cas.global.consensus;

import cn.primeledger.cas.global.blockchain.Block;

/**
 * @author yuguojia
 * @date 2018/03/05
 **/
public class SignBlockScoreStrategy {
    public static int MAX_SIG_SCORE = 10000;
    public static int MAX_HIGH = 10000;

    public static int calcSignScore(Block block, Block signerBlock) {
        long blockHeight = block.getHeight();
        long signerBlockHeight = signerBlock.getHeight();
        if (blockHeight <= signerBlockHeight) {
            return 0;
        }
        int highGap = (int) (blockHeight - signerBlockHeight);
        //2<<-1=0,2<<0=2,2<<1=4,...
        int score = MAX_SIG_SCORE - 2 << (highGap - 2);
        return score;
    }

    public static void main(String[] arg) {
        System.out.println(2 << 30);
    }
}