package com.higgsblock.global.chain.app.service.impl;

import com.higgsblock.global.chain.app.BaseMockTest;
import com.higgsblock.global.chain.app.blockchain.Block;
import com.higgsblock.global.chain.app.blockchain.listener.MessageCenter;
import com.higgsblock.global.chain.app.service.IVoteService;
import com.higgsblock.global.chain.app.service.IWitnessService;
import com.higgsblock.global.chain.crypto.KeyPair;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.powermock.api.mockito.PowerMockito;

/**
 * @author yangshenghong
 * @date 2018-10-15
 */
public class OriginalBlockServiceTest extends BaseMockTest {
    @InjectMocks
    private OriginalBlockService originalBlockService;

    @Mock
    private KeyPair keyPair;
    @Mock
    private IWitnessService witnessService;
    @Mock
    private MessageCenter messageCenter;
    @Mock
    private IVoteService voteService;

    @Test
    public void sendOriginBlockToWitness() {
        //The current address is not witness
        Block block = new Block();
        PowerMockito.when(witnessService.isWitness(keyPair.getAddress())).thenReturn(false);
        originalBlockService.sendOriginBlockToWitness(block);

        //The current address is s witness
        PowerMockito.when(witnessService.isWitness(keyPair.getAddress())).thenReturn(true);
        originalBlockService.sendOriginBlockToWitness(block);
    }
}