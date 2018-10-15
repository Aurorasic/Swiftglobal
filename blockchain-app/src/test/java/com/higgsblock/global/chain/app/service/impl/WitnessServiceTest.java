package com.higgsblock.global.chain.app.service.impl;

import com.google.common.collect.Lists;
import com.higgsblock.global.chain.app.BaseMockTest;
import com.higgsblock.global.chain.app.net.peer.Peer;
import com.higgsblock.global.chain.app.net.peer.PeerManager;
import com.higgsblock.global.chain.app.service.IBlockChainInfoService;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.powermock.api.mockito.PowerMockito;

import java.util.List;

/**
 * @author yangshenghong
 * @date 2018-10-15
 */
public class WitnessServiceTest extends BaseMockTest {
    @InjectMocks
    private WitnessService witnessService;

    @Mock
    private IBlockChainInfoService blockChainInfoService;
    @Mock
    private PeerManager peerManager;

    @Test
    public void afterPropertiesSet() throws Exception {
        //get witnessPeer is empty
        List<Peer> peers = Lists.newArrayList();
        PowerMockito.when(witnessService.getAllWitnessPeer()).thenReturn(peers);
        witnessService.afterPropertiesSet();

        //get witnessPeer is not empty
        peers.add(new Peer());
        PowerMockito.when(witnessService.getAllWitnessPeer()).thenReturn(peers);
        witnessService.afterPropertiesSet();
    }

    @Test
    public void getAllWitnessPeer() throws Exception {
        witnessService.getAllWitnessPeer();
    }

    @Test
    public void isWitness() throws Exception {
        String address = "address";
        witnessService.isWitness(address);
    }

    @Test
    public void getWitnessSize() throws Exception {
        witnessService.getWitnessSize();
    }
}