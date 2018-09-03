package com.higgsblock.global.chain.app.blockchain;

import com.alibaba.fastjson.annotation.JSONType;
import com.higgsblock.global.chain.common.entity.BaseSerializer;
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
@JSONType(includes = {"version", "sourceId", "block"})
public class BlockFullInfo extends BaseSerializer {
    private int version;
    private String sourceId;
    private Block block;
}