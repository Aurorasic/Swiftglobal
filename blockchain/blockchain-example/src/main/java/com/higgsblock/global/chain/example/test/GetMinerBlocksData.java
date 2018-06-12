package com.higgsblock.global.chain.example.test;

import com.alibaba.fastjson.JSON;
import com.higgsblock.global.chain.app.api.vo.MinerBlock;
import com.higgsblock.global.chain.app.api.vo.ResponseData;
import com.higgsblock.global.chain.app.constants.RespCodeEnum;
import com.higgsblock.global.chain.crypto.ECKey;
import com.higgsblock.global.chain.example.api.IMinerApi;
import com.higgsblock.global.chain.network.http.HttpClient;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.util.List;

/**
 * @author kongyu
 * @date 2018-5-30 11:16
 */
public class GetMinerBlocksData {
    private final static String IP = "127.0.0.1";
    private final static int PORT = 8084;
    /**转出公钥*/
    private static String priKey = "a2fc438939010929b8a0bc71ef432a22157eaa1d452eff3aab5d28ff29f42580";
    private static String pubKey = "03d5eb9f503d18e9c5d998e4dd9a3b5e43b7dccfa20957c7ee32ede8117d1a1a10";

    public static void main(String[] args) throws Exception {
        List<MinerBlock> minerBlocks = isMinerBlockDataByPubKey(IP, PORT, ECKey.fromPrivateKey(priKey).getKeyPair().getPubKey());

        System.out.println("-----------------------------------------------");
        if (CollectionUtils.isEmpty(minerBlocks)) {
            System.out.println("size is 0");
            return;
        }
        for (MinerBlock minerBlock : minerBlocks) {
            System.out.println(JSON.toJSONString(minerBlock, true));
        }
    }

    public static List<MinerBlock> isMinerBlockDataByPubKey(String ip, int port, String pubKey) {
        if (null == ip || null == pubKey) {
            throw new RuntimeException("params is null");
        }
        if (port <= 0) {
            throw new RuntimeException("port is invalid");
        }

        IMinerApi api = HttpClient.getApi(ip, port, IMinerApi.class);
        ResponseData<List<MinerBlock>> responseData = null;
        try {
            responseData = api.statisticsMinerBlocks(pubKey).execute().body();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        if (StringUtils.equals(RespCodeEnum.SUCCESS.getCode(), responseData.getRespCode())) {
            return responseData.getData();
        }
        return null;
    }
}
