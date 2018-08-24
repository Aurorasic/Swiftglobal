package com.higgsblock.global.chain.network.socket.event;

import com.alibaba.fastjson.annotation.JSONType;
import com.higgsblock.global.chain.common.entity.BaseSerializer;
import com.higgsblock.global.chain.network.socket.constants.ChannelType;
import io.netty.channel.Channel;
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
@JSONType(includes = {"channel", "type"})
public class CreateChannelEvent extends BaseSerializer {

    protected Channel channel;
    protected ChannelType type;

}
