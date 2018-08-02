package com.higgsblock.global.chain.app.common;

/**
 * @author HuangShengli
 * @date 2018-08-01
 **/
public enum ScoreRangeEnum {
    /**
     * level 5 score range
     */
    LEVEL5_SCORE(1200, Integer.MAX_VALUE, 3),
    /**
     * level 4 score range
     */
    LEVEL4_SCORE(1000, 1200, 2),
    /**
     * level 3 score range
     */
    LEVEL3_SCORE(800, 1000, 1),
    /**
     * level 2 score range
     */
    LEVEL2_SCORE(600, 800, 1),
    /**
     * level 1 score range
     */
    LEVEL1_SCORE(0, 600, 0);

    private int minScore;
    private int maxScore;
    /**
     * need to select miner num
     */
    private int selectSize;

    public int getMinScore() {
        return minScore;
    }

    public int getMaxScore() {
        return maxScore;
    }

    public int getSelectSize() {
        return selectSize;
    }

    ScoreRangeEnum(int minScore, int maxScore, int selectSize) {
        this.minScore = minScore;
        this.maxScore = maxScore;
        this.selectSize = selectSize;
    }
}
