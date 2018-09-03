package com.higgsblock.global.chain.network.socket.event;

import com.alibaba.fastjson.annotation.JSONType;
import com.higgsblock.global.chain.common.entity.BaseSerializer;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


/**
 * @author baizhengwen
 * @date 2018/2/28
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@JSONType(includes = {"channelId"})
public class DiscardChannelEvent extends BaseSerializer {

    protected String channelId;

}
