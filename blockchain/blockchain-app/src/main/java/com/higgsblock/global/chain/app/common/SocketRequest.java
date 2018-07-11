package com.higgsblock.global.chain.app.common;

import com.higgsblock.global.chain.common.entity.BaseSerializer;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author baizhengwen
 * @date 2018/3/27
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class SocketRequest<T> extends BaseSerializer {

    private String sourceId;
    private T data;
}
