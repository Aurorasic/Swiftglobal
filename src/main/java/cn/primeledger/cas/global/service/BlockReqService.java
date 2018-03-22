package cn.primeledger.cas.global.service;

import cn.primeledger.cas.global.blockchain.Block;
import cn.primeledger.cas.global.consensus.sign.model.WitnessSign;
import cn.primeledger.cas.global.network.http.client.HttpClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Slf4j
@Service
public class BlockReqService {

    public WitnessSign getWitnessSign(String ip, int port, Block block) {
        IBlockApi api = HttpClient.getApi(ip, port, IBlockApi.class);

        try {
            WitnessSign sign = api.getWitnessSign(block).execute().body();

            return sign;
        } catch (IOException e) {
            LOGGER.error("getWitnessSign Error: ", e);
            return null;
        }
    }

}
