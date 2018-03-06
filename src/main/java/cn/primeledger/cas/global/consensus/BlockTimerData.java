package cn.primeledger.cas.global.consensus;

import cn.primeledger.cas.global.blockchain.Block;

import java.util.ArrayList;
import java.util.List;

/**
 * @author yuanjiantao
 * @date Created on 3/6/2018
 */
public class BlockTimerData {

    private Long firstHeight;

    private List<Long> time;

    private List<String> pubKeys;

    private List<Boolean> states;


    public BlockTimerData() {
        // TODO: 3/6/2018 yuanjiantao init the firstHeight
        firstHeight = 0L;
        time = new ArrayList<>();
        pubKeys = new ArrayList<>();
        states = new ArrayList<>();
    }

    public void receiveBlock(Block block) {
        int index = index(block.getHeight());
        if (index != -1 && !(states.get(index) != null && states.get(index).compareTo(Boolean.TRUE) == 0)) {
            states.set(index, true);
            time.set(index, System.currentTimeMillis());
            String pubKey = block.getMinerPKSig().getPubKey();
            if (!pubKey.equals(pubKeys.get(index))) {
                // TODO: 3/6/2018 yuanjiantao two different pubKeys  

                pubKeys.remove(index);
                // TODO: 3/6/2018  yuanjiantao verify the pubkey is correct
            }
        }
    }

    private int index(Long height) {
        Long index = height - firstHeight;
        if (index > -1) {
            return index.intValue();
        } else {
            return -1;
        }
    }

    public long getTimeByHeight(long height) {
        int index = index(height);
        if (index != -1) {
            return time.get(index);
        } else {
            return -1L;
        }
    }

}
