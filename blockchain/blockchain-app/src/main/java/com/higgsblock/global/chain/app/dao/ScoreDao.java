package com.higgsblock.global.chain.app.dao;

import org.springframework.stereotype.Repository;

/**
 * @author HuangShengli
 * @date 2018-05-22
 */
@Repository
public class ScoreDao extends BaseDao<String, Integer> {


    @Override
    public String getColumnFamilyName() {
        return "score";
    }
}