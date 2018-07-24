package com.higgsblock.global.chain.network.socket.message;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author baizhengwen
 * @date 2018-07-24
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public abstract class BaseMessage<T> implements IMessage<T> {
    private String sourceId;
    private T data;
}
