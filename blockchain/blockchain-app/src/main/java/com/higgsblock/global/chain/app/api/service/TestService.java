package com.higgsblock.global.chain.app.api.service;

import com.higgsblock.global.chain.app.dao.IStudentDao;
import com.higgsblock.global.chain.app.dao.entity.Student;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.math.RandomUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author baizhengwen
 * @date 2018/4/20
 */
@Slf4j
@Deprecated
@Service
public class TestService {

    @Autowired
    private IStudentDao userDao;

    @Transactional
    public void batchAdd() {
        boolean cancel = RandomUtils.nextBoolean();
        Student user;
        for (int i = 0; i < 10; i++) {
            user = new Student();
            user.setName("name_" + i);
            user.setAge(10 + i);
            userDao.add(user);
            LOGGER.info("add user, index={}", i);
        }
        if (cancel) {
            throw new RuntimeException("cancel..");
        }
    }

}
