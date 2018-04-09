package cn.primeledger.cas.global.common;

import java.io.Serializable;

/**
 * @author yuguojia
 * @date 2018/03/29
 **/
public enum SystemStatus implements Serializable {

    INI(1, "初始化"),
    LOADING(2, "加载所有区块数据"),
    SYNC_BLOCKS(3, "区块数据同步"),
    RUNNING(4, "正常运行");

    private int state;
    private String desc;

    SystemStatus(int state, String desc) {
        this.state = state;
        this.desc = desc;
    }

    public int getState() {
        return state;
    }

    public String getDesc() {
        return desc;
    }

    public boolean equals(SystemStatus otherState) {
        if (this.state == otherState.state) {
            return true;
        }
        return false;
    }
}