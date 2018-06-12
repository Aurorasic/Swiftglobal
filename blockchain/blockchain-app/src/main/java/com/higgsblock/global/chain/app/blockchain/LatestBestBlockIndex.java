package com.higgsblock.global.chain.app.blockchain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author zhaoxiaogang
 * @date 2018-05-21
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class LatestBestBlockIndex {
    private long height;
    private String bestBlockHash;
}
