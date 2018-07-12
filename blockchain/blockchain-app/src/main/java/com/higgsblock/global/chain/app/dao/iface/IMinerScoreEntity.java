package com.higgsblock.global.chain.app.dao.iface;


import java.util.List;

/**
 * @author yangshenghong
 * @date 2018-05-08
 */
public interface IMinerScoreEntity {

    /**
     * Add them according to different tables.
     *
     * @param minerScoreEntity
     * @param table            table name
     * @return
     */
    int add(String table, MinerScoreEntity minerScoreEntity);

    /**
     * update them according to different tables.
     *
     * @param minerScoreEntity
     * @param table            table name
     * @return
     */
    int update(String table, MinerScoreEntity minerScoreEntity);

    /**
     * delete them according to different tables.
     *
     * @param address
     * @param table   table name
     * @return
     */
    int delete(String table, String address);

    /**
     * Query the table according to the field.
     *
     * @param address
     * @param table   table name
     * @return
     */
    MinerScoreEntity getByField(String table, String address);

    /**
     * Query all data from table.
     *
     * @param table
     * @return
     */
    List<MinerScoreEntity> findAll(String table);

    /**
     * batch insert
     *
     * @param tableName
     * @param minerScoreEntities The data set to be inserted.
     * @return
     */
    int[] batchInsert(String tableName, List<MinerScoreEntity> minerScoreEntities);

    /**
     * delete all data
     *
     * @param tableName
     * @return
     */
    int deleteAll(String tableName);
}
