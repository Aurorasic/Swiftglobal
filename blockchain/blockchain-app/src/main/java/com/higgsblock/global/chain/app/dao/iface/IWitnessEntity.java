package com.higgsblock.global.chain.app.dao.iface;

import com.higgsblock.global.chain.app.dao.entity.WitnessPo;

import java.util.List;

/**
 * @author yangshenghong
 * @date 2018-06-30
 */
public interface IWitnessEntity extends IDao<WitnessPo>{

    List<WitnessPo> getByHeight(long height);

    int[] batchInsert(List<WitnessPo> witnessEntities);

}
