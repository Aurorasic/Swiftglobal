package cn.primeledger.cas.global.p2p;

import cn.primeledger.cas.global.p2p.message.HelloMessage;
import cn.primeledger.cas.global.p2p.message.HelloWraper;
import cn.primeledger.cas.global.utils.ProtoBufUtil;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

public class PeerTest {

    @Test
    public void asdasd() {

        HelloMessage helloMessage1 = new HelloMessage(new HelloWraper("1", 8000, 123123, 0));
        HelloMessage helloMessage2 = new HelloMessage(new HelloWraper("1", 8000, 123123, 0));


        List list = new ArrayList();
        list.add(helloMessage1);
        list.add(helloMessage2);

        byte[] data = ProtoBufUtil.serialize(list);
        System.out.println(data.length);
        String str = "1" + "2" + 123123 + "";
        System.out.println(str.getBytes().length);
        System.out.println(ProtoBufUtil.serialize(str).length);
        System.out.println(ProtoBufUtil.serialize(helloMessage1).length);

        List deset = ProtoBufUtil.deserialize(data);
        System.out.println(deset);
        System.out.println("asd");

    }

}