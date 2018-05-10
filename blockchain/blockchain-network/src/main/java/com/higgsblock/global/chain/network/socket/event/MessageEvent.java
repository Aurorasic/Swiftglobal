package com.higgsblock.global.chain.network.socket.event;

import com.higgsblock.global.chain.common.entity.BaseSerializer;
import lombok.Data;

/**
 * @author baizhengwen
 * @date 2018/2/28
 */
@Data
public class MessageEvent extends BaseSerializer {

    protected String sourceId;
    protected String content;

}
