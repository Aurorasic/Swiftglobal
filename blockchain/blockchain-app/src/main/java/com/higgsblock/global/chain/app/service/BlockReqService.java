package com.higgsblock.global.chain.app.service;

import com.higgsblock.global.chain.app.blockchain.Block;
import com.higgsblock.global.chain.network.http.HttpClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Collection;
import java.util.LinkedList;

/**
 * @author yuguojia
 * @date 2018/4/26
 */
@Slf4j
@Service
public class BlockReqService {

    public Block getRecommendBlock(String ip, int port, long height) {
        IWitnessApi api = HttpClient.getApi(ip, port, IWitnessApi.class);
        try {
            Block block = api.getRecommendBlock(height).execute().body();
            return block;
        } catch (IOException e) {
            LOGGER.error("getSignedBlock Error: ", e);
            return null;
        }
    }

    public Boolean sendBlockToWitness(String ip, int port, Block block) {
        IWitnessApi api = HttpClient.getApi(ip, port, IWitnessApi.class);
        try {
            return api.sendBlockToWitness(block).execute().body();
        } catch (IOException e) {
            LOGGER.error("sendBlockToWitness Error: ", e);
            return Boolean.TRUE;
        }
    }

    Collection<String> getCandidateBlockHashs(String ip, int port, long height) {
        IWitnessApi api = HttpClient.getApi(ip, port, IWitnessApi.class);
        try {
            Collection<String> result = api.getCandidateBlockHashs(height).execute().body();
            return result;
        } catch (IOException e) {
            LOGGER.error("getCandidateBlockHashs error: ", e);
        }
        return new LinkedList<>();
    }

    Collection<Block> getCandidateBlocksByHashs(String ip, int port, Collection<String> blockHashs) {
        IWitnessApi api = HttpClient.getApi(ip, port, IWitnessApi.class);
        try {
            Collection<Block> result = api.getCandidateBlocksByHashs(blockHashs).execute().body();
            return result;
        } catch (IOException e) {
            LOGGER.error("getCandidateBlocksByHashs error: ", e);
        }
        return new LinkedList<>();
    }

    Boolean putBlocksToWitness(String ip, int port, Collection<Block> blocks) {
        IWitnessApi api = HttpClient.getApi(ip, port, IWitnessApi.class);
        try {
            Boolean result = api.putBlocksToWitness(blocks).execute().body();
            return result;
        } catch (IOException e) {
            LOGGER.error("putBlocksToWitness error: ", e);
        }
        return false;
    }

}
