package com.higgsblock.global.chain.app.dao.impl;

import com.google.common.collect.ImmutableMap;
import com.higgsblock.global.chain.app.dao.entity.PeerEntity;
import com.higgsblock.global.chain.app.dao.iface.IPeerEntity;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * @author yangshenghong
 * @date 2018-05-08
 */
@Repository
public class PeerEntityDao extends BaseDao<PeerEntity> implements IPeerEntity {
    @Override
    public int add(PeerEntity peerEntity) {
        String sql = "insert into t_peer values (:pubKey,:id,:ip,:socketPort,:httpPort,:version,:signature,:retry);";
        return super.add(peerEntity, sql);
    }

    @Override
    public int update(PeerEntity peerEntity) {
        String sql = "update t_peer set pub_key=:pubKey,id = :id,ip=:ip,socket_port=:socketPort,http_port=:httpPort,version=:version,signature=:signature,retry=:retry where id = :id";
        return super.update(peerEntity, sql);
    }

    @Override
    public <E> int delete(E id) {
        String sql = "delete from t_peer where id=:id";
        return super.delete(sql, ImmutableMap.of("id", id));
    }

    @Override
    public <E> PeerEntity getByField(E parameter) {
        String sql = "select pub_key,id,ip,socket_port,http_port,version,signature,retry from t_peer where id=:id";
        return super.getByField(sql, ImmutableMap.of("id", parameter));
    }

    @Override
    public Integer getCount() {
        String sql = "select count(0) from t_peer";
        Integer integer = super.template.getJdbcOperations().queryForObject(sql, Integer.class);
        return integer;
    }

    @Override
    public List<PeerEntity> findAll() {
        String sql = "select pub_key,id,ip,socket_port,http_port,version,signature,retry from t_peer";
        return super.findAll(sql);
    }

    @Override
    public int updateRetriesById(PeerEntity peerEntity) {
        String sql = "update t_peer set retry=:retry where id = :id";
        return super.update(peerEntity, sql);
    }

    @Override
    public int deleteAll() {
        String sql = "delete from t_peer";
        return super.delete(new PeerEntity(), sql);
    }
}
