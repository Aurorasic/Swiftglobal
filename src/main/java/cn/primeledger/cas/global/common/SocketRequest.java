package cn.primeledger.cas.global.common;

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
public class SocketRequest<T> {

    private String sourceId;
    private T data;
}
