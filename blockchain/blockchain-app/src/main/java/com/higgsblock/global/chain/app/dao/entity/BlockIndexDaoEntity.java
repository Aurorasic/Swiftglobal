package com.higgsblock.global.chain.app.dao.entity;

import lombok.Data;

import java.util.List;

/**
 * @author Zhao xiaogang
 * @date 2018-05-22
 */
@Data
public class BlockIndexDaoEntity {
    private List<BaseDaoEntity> baseDaoEntity;
    private boolean createUtxo;
}
