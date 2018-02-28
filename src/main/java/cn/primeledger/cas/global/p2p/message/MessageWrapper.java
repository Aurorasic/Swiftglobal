package cn.primeledger.cas.global.p2p.message;

import lombok.Getter;
import lombok.Setter;

/**
 * @author yuanjiantao
 * @since 2018-02-26
 **/
@Getter
@Setter
public class MessageWrapper {

    private final BaseMessage msg;
    private long lastTimestamp = 0;
    private long retryTimes = 0;
    private boolean answered = false;

    public MessageWrapper(BaseMessage msg) {
        this.msg = msg;
        saveTime();
    }

    public void saveTime() {
        lastTimestamp = System.currentTimeMillis();
    }

    public boolean hasToRetry() {
        return 20000 < System.currentTimeMillis() - lastTimestamp;
    }

    public void incRetryTimes() {
        ++retryTimes;
    }

}
