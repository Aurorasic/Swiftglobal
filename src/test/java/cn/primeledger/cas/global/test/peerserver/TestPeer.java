package cn.primeledger.cas.global.test.peerserver;

import cn.primeledger.cas.global.network.http.client.HttpClient;
import cn.primeledger.cas.global.p2p.Peer;
import cn.primeledger.cas.global.service.IPeerApi;
import com.alibaba.fastjson.JSON;

import java.io.IOException;

public class TestPeer {
    public static void main(String[] args) throws IOException {
        IPeerApi api = HttpClient.getApi("localhost", 8081, IPeerApi.class);

        Peer peer = new Peer();
        api.register(peer).execute().body();
        System.out.println(JSON.toJSONString(peer, true));
    }
}
