package com.higgsblock.global.chain.app.context;

import org.springframework.stereotype.Component;

/**
 * @author baizhengwen
 * @date 2018/3/22
 */
@Component
public class AppContext {

    public void start() {
        // 启动本地socket服务端（连接进来的peer必须在一定时间内上报自己的peer信息，服务端才会保持长连接，否则会在超时后丢弃）
        startSocketServer();
        // 从注册中心获取peer信息
        getPeers();
        // 作为客户端尝试连接其他peer服务端（并在连接建立之后，上报自己的peer信息，收到服务端响应后则保持长连接，否则可能会被丢弃）
        startSocketClient();
        // 加载本节点所有区块，并计算索引等相关数据
        loadData();
        // 询问相邻节点区块高度，同步区块
        syncData();
        // 开始挖矿
        startMining();
    }

    private void startSocketServer() {
        // todo baizhengwen 启动本地socket服务端
    }

    private void getPeers() {
        // todo baizhengwen 从注册中心获取peer信息
    }

    private void startSocketClient() {
        // todo baizhengwen 作为客户端尝试连接其他peer服务端
    }

    private void syncData() {
        // todo baizhengwen 同步区块
    }

    private void loadData() {
        // todo baizhengwen 加载本节点所有区块
    }

    private void startMining() {
        // todo baizhengwen 开始挖矿
    }

}
