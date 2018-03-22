package cn.primeledger.cas.global.context;

import org.springframework.stereotype.Component;

/**
 * @author baizhengwen
 * @date 2018/3/22
 */
@Component
public class AppContext {

    public void start() {
        // 获取公网ip
        // 如果支持upnp，则进行端口映射
        // 启动本地socket服务端（连接进来的peer必须在一定时间内上报自己的peer信息，服务端才会保持长连接，否则会在超时后丢弃）
        // 从注册中心获取peer信息
        // 作为客户羊尝试连接其他peer服务端（并在连接建立之后，上报自己的peer信息，收到服务端响应后则保持长连接，否则可能会被丢弃）
        // 询问相邻节点区块高度，并开始同步区块qq
        // 同步区块完成后，加载本节点所有区块，并计算索引等相关数据
        // 开始挖矿
    }

//    public void

    public void syncData() {

    }

//    public void

}
