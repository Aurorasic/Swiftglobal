package com.higgsblock.global.browser.service.iface;

import com.higgsblock.global.browser.dao.entity.BlockHeaderPO;
import com.higgsblock.global.browser.dao.entity.MinerPO;
import com.higgsblock.global.browser.dao.entity.RewardPO;
import com.higgsblock.global.browser.dao.entity.UTXOPO;
import com.higgsblock.global.browser.service.bo.BlockBO;

import java.util.List;
import java.util.Map;

/**
 * @author yangshenghong
 * @date 2018-05-24
 */
public interface IBlockService {
    /**
     * Get the block according to the hash.
     *
     * @param blockHash
     * @return
     */
    BlockBO getBlockByHash(String blockHash);

    /**
     * save block
     *
     * @param blockHeaderPo
     * @param transaction
     * @param rewardPo
     * @param miners
     * @param address
     * @param utxoPos
     */
    void saveBlock(BlockHeaderPO blockHeaderPo, Map<String, Object> transaction, List<RewardPO> rewardPo,
                   List<MinerPO> miners, List<String> address, List<UTXOPO> utxoPos);
}
