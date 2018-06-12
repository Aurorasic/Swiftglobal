package com.higgsblock.global.chain.app.blockchain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * @author yuguojia
 * @date 2018/03/12
 **/
@Data
@NoArgsConstructor
@AllArgsConstructor
public class BlockFullInfo implements Serializable {
    private short version;
    private String sourceId;
    private Block block;
}