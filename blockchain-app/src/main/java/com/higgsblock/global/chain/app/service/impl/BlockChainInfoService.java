package com.higgsblock.global.chain.app.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Lists;
import com.higgsblock.global.chain.app.dao.IBlockChainInfoRepository;
import com.higgsblock.global.chain.app.dao.IWitnessRepository;
import com.higgsblock.global.chain.app.dao.entity.BlockChainInfoEntity;
import com.higgsblock.global.chain.app.dao.entity.WitnessEntity;
import com.higgsblock.global.chain.app.net.peer.Peer;
import com.higgsblock.global.chain.app.service.IBlockChainInfoService;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author baizhengwen
 * @date 2018-09-06
 */
@Service
public class BlockChainInfoService implements IBlockChainInfoService {

    private static final String KEY_MAX_HEIGHT = "MaxHeight";
    private static final String KEY_ALL_SCORES = "allScores";
    private static final String KEY_ALL_WITNESS = "allWitness";

    @Autowired
    private IBlockChainInfoRepository blockChainInfoRepository;

    @Autowired
    private IWitnessRepository witnessRepository;

    @Autowired
    private IBlockChainInfoService blockChainInfoService;

    @Override
    public long getMaxHeight() {
        return NumberUtils.toLong(blockChainInfoRepository.findOne(KEY_MAX_HEIGHT).getValue());
    }

    @Override
    public void setMaxHeight(long height) {
        BlockChainInfoEntity entity = new BlockChainInfoEntity(KEY_MAX_HEIGHT, String.valueOf(height));
        blockChainInfoRepository.save(entity);
    }

    @Override
    public Map<String, String> getAllScores() {
        BlockChainInfoEntity chainInfoEntity = blockChainInfoRepository.findOne(KEY_ALL_SCORES);
        if (chainInfoEntity == null ||
                StringUtils.isEmpty(chainInfoEntity.getValue())) {
            return new HashMap<String, String>();
        }
        JSONObject jsonObject = JSONObject.parseObject(chainInfoEntity.getValue());
        Map<String, String> result = (Map) jsonObject;
        return result;
    }

    @Override
    public void setAllScores(Map<String, String> allScores) {
        String scores = JSON.toJSONString(allScores);
        BlockChainInfoEntity entity = new BlockChainInfoEntity(KEY_ALL_SCORES, scores);
        blockChainInfoRepository.save(entity);
    }

    @Override
    public void setAllWitness(String allWitnesss) {
        BlockChainInfoEntity entity = new BlockChainInfoEntity(KEY_ALL_WITNESS, allWitnesss);
        blockChainInfoRepository.save(entity);
    }

    @Override
    public void deleteAllScores() {
        blockChainInfoRepository.delete(KEY_ALL_SCORES);
    }

    @Override
    public List<Peer> getAllWitness() {
        BlockChainInfoEntity chainInfoEntity = blockChainInfoRepository.findOne(KEY_ALL_WITNESS);
        if (chainInfoEntity == null || StringUtils.isEmpty(chainInfoEntity.getValue())) {
            return new ArrayList<>(0);
        }
        List<WitnessEntity> list = JSONObject.parseArray(chainInfoEntity.getValue(), WitnessEntity.class);
        List<Peer> peers = Lists.newArrayList();
        list.forEach(witnessEntity -> {
            peers.add(witnessEntity2Peer(witnessEntity));
        });
        return peers;
    }

    private static Peer witnessEntity2Peer(WitnessEntity witnessEntity) {
        if (witnessEntity == null) {
            return null;
        }
        Peer peer = new Peer();
        peer.setIp(witnessEntity.getAddress());
        peer.setSocketServerPort(witnessEntity.getSocketPort());
        peer.setHttpServerPort(witnessEntity.getHttpPort());
        peer.setPubKey(witnessEntity.getPubKey());
        return peer;
    }
}
