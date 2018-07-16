package com.higgsblock.global.chain.app.api.vo;

import com.higgsblock.global.chain.common.entity.BaseSerializer;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

/**
 * @author huangshengli
 * @since 2018-07-16
 */
@Data
@NoArgsConstructor
public class DposGroupVO extends BaseSerializer {

    /**
     * the round of dpos nodes
     */
    private long sn;

    /**
     * the start height of this round
     */
    private long startHeight;
    /**
     * the end height of this round
     */
    private long endHeight;

    /**
     * the addresses of selected dpos nodes within this round
     */
    private List<String> dposNodes;
    /**
     * the addresses of left dpos nodes within this round
     */
    private List<String> leftDposNodes;

    private List<SimpleBlockVO> blockVOS = new ArrayList<>(5);

}
