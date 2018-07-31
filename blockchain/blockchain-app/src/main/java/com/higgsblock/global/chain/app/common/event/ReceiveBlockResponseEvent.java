package com.higgsblock.global.chain.app.common.event;

import com.higgsblock.global.chain.common.entity.BaseSerializer;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author yuguojia
 * @date 2018/04/04
 **/
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReceiveBlockResponseEvent extends BaseSerializer {
    private long height;
}