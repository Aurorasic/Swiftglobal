package com.higgsblock.global.chain.app.dao.iface;

import com.higgsblock.global.chain.dao.entity.User;

/**
 * @author baizhengwen
 * @date 2018-04-27
 */
@Deprecated
public interface ITestDao {

    void add(User user);

    User getByNo(String no);
}
