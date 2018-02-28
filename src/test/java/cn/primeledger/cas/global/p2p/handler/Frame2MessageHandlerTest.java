package cn.primeledger.cas.global.p2p.handler;

import cn.primeledger.cas.global.p2p.message.BaseMessage;
import cn.primeledger.cas.global.p2p.message.PingMessage;
import org.junit.Test;
import org.spongycastle.util.encoders.Hex;
import org.xerial.snappy.Snappy;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static java.lang.Math.min;

public class Frame2MessageHandlerTest {


//    public static final int NO_FRAMING = Integer.MAX_VALUE >> 1;

    private int maxFramePayloadSize = 5;

    AtomicInteger contextIdCounter = new AtomicInteger(1);

    @Test
    public void encode() throws IOException {

        //create a message

        //get bytes
        byte[] data = Hex.decode("ABCDCBAD1CABCDCBAD1CABCDCBAD1CABCDCBAD1CABCDCBAD1CABCDCBAD1C");

        //get compressed bytes
        byte[] bytes = Snappy.compress(data);

        // compressed bytes message to frame
        int cmd = 11;
        List<FrameHandler.Frame> ret = new ArrayList<>();
        int curPos = 0;

        System.out.println("data length : " + data.length);
        System.out.println("compressed data  length : " + bytes.length);

        while (curPos < bytes.length) {
            int newPos = min(curPos + maxFramePayloadSize, bytes.length);
            byte[] frameBytes = curPos == 0 && newPos == bytes.length ? bytes :
                    Arrays.copyOfRange(bytes, curPos, newPos);
            ret.add(new FrameHandler.Frame(cmd, bytes.length, frameBytes));
            curPos = newPos;
            System.out.println("frame size : " + frameBytes.length);
        }


    }

    @Test
    public void decode() {
    }

    @Test
    public void decodeMessage() {
    }


}