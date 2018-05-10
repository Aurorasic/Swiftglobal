package com.higgsblock.global.chain.app.blockchain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author yuguojia
 * @date 2018/03/12
 **/
@Data
@NoArgsConstructor
@AllArgsConstructor
public class BlockFullInfo {
    private short version;
    private String sourceId;
    private Block block;
}