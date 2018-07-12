package com.higgsblock.global.chain.app.dao.iface;

import com.higgsblock.global.chain.app.dao.entity.WitnessEntity;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * @author yangshenghong
 * @date 2018-07-12
 */
public interface IWitnessRepository extends JpaRepository<WitnessEntity, Integer> {
}
