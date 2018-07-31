package com.higgsblock.global.chain.app.common.event;

import com.higgsblock.global.chain.common.entity.BaseSerializer;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author yuanjiantao
 * @date 7/31/2018
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReceiveBlockResponseEvent extends BaseSerializer {
    private long height;
}