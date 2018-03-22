package cn.primeledger.cas.global.p2p.handler;

import cn.primeledger.cas.global.utils.ProtoBufUtil;
import org.junit.Test;
import org.xerial.snappy.Snappy;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class ClientInboundHandlerTest {

    private int maxFramePayloadSize = 5;

    AtomicInteger contextIdCounter = new AtomicInteger(1);

    @Test
    public void encode() throws IOException {


        List list = new ArrayList();
        for (int i = 0; i < 1000; i++) {
            list.add("" + i);
        }

        //get bytes by protostuff
        byte[] data = ProtoBufUtil.serialize(list);

        //get compressed bytes
        byte[] bytes = Snappy.compress(data);

        // compressed bytes message to frame
        int cmd = 11;
        List<FrameCodecHandler.Frame> ret = new ArrayList<>();
        int curPos = 0;

//        System.out.println("size :" + getBytes().length);
        System.out.println("data length : " + data.length);
        System.out.println("compressed data  length : " + bytes.length);

//        SerializeDeserializeWrapper wrapper = SerializeDeserializeWrapper.builder(list);
        List<String> list1 = ProtoBufUtil.deserialize(data);
//        SerializeDeserializeWrapper deserializeWrapper = ProtoBufUtil.deserialize(data);
//        List<String> list1 = (ArrayList) deserializeWrapper.getData();
//        List a = new ArrayList(String);
//        System.out.println(deserializeWrapper.getData());
        System.out.println(list1);

//        while (curPos < bytes.length) {
//            int newPos = min(curPos + maxFramePayloadSize, bytes.length);
//            byte[] frameBytes = curPos == 0 && newPos == bytes.length ? bytes :
//                    Arrays.copyOfRange(bytes, curPos, newPos);
//            ret.add(new FrameCodecHandler.Frame(cmd, bytes.length, frameBytes));
//            curPos = newPos;
//            System.out.println("frame size : " + frameBytes.length);
//        }
        System.out.println("end");
    }

    @Test
    public void decode() {
    }

    @Test
    public void decodeMessage() {
    }


}