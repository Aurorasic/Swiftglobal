package com.higgsblock.global.chain.app.dao;

import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * @author yangyi
 * @date 2018/5/24
 * @description
 */
@Repository
public class DposDao extends BaseDao<Long, String> {

    private static final String COLUMN_FAMILY_NAME = "dpos";

    @Override
    protected String getColumnFamilyName() {
        return DposDao.COLUMN_FAMILY_NAME;
    }
}
