package com.higgsblock.global.chain.app.dao;

        import com.higgsblock.global.chain.app.blockchain.Block;
        import org.springframework.stereotype.Repository;

/**
 * DAO for block
 *
 * @author zhao xiaogang
 * @date 2018-05-21
 */

@Repository
public class BlockDao extends BaseDao<String, Block> {

    @Override
    protected String getColumnFamilyName() {
        return "block";
    }

}
