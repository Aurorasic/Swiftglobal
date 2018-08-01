package com.higgsblock.global.chain.app.common;

/**
 * @author HuangShengli
 * @date 2018-08-01
 **/
public enum ScoreRangeEnum {
    /**
     * level 1 score range
     */
    MAX_SCORE(1000, Integer.MAX_VALUE, 3),
    /**
     * level 2 score range
     */
    MID_SCORE(800, 1000, 2),
    /**
     * level 3 score range
     */
    MIN_SCORE(600, 800, 1),
    /**
     * level bottom score range
     */
    BOTTOM_SCORE(0, 600, 0);

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
