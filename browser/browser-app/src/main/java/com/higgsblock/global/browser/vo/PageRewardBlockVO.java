package com.higgsblock.global.browser.vo;

import lombok.Data;

import java.util.List;

/**
 * @author Su Jiulong
 * @date 2018-05-24
 */
@Data
public class PageRewardBlockVO {
    /**
     * The record content of pages.
     */
    private List<RewardBlockVO> items;
    /**
     * total count
     */
    private long countTotal;
}
