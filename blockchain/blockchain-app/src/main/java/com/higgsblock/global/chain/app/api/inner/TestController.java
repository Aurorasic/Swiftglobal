package com.higgsblock.global.chain.app.api.inner;

import com.higgsblock.global.chain.app.api.service.TestService;
import com.higgsblock.global.chain.app.blockchain.Block;
import com.higgsblock.global.chain.app.dao.BlockDao;
import com.higgsblock.global.chain.app.dao.IStudentDao;
import com.higgsblock.global.chain.app.dao.entity.Student;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * @author baizhengwen
 * @create 2018-03-17
 */
@Deprecated
@RequestMapping("/test")
@RestController
@Slf4j
public class TestController {

    @Autowired
    private IStudentDao userDao;
    @Autowired
    private TestService service;

    @Autowired
    private BlockDao blockDao;

    @RequestMapping("/addblock")
    public boolean add(String hash) {
        Block block = new Block();
        block.setHash(hash);
        block.setHeight(1);
        block.setVersion((short)1);
        block.setBlockTime(1);
        block.setPrevBlockHash("b");

        try {
            blockDao.addBlock(block);
        } catch (Exception e) {
            return false;
        }

        return true;
    }

    @RequestMapping("/getblock")
    public Block getBlock(String hash) {
        try {
            return blockDao.getBlock(hash);
        } catch (Exception e) {
            return new Block();
        }
    }

    @RequestMapping("/delblock")
    public boolean delBlock(String hash) {
        try {
            blockDao.delBlock(hash);
        } catch (Exception e) {
            return false;
        }

        return true;
    }


    @RequestMapping("/add")
    public boolean add(String name, int age) {
        Student user = new Student();
        user.setName(name);
        user.setAge(age);
        return userDao.add(user);
    }

    @RequestMapping("/tx")
    public List<Student> addBatch() {
        try {
            service.batchAdd();
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
        }
        return userDao.list(0, 100);
    }

    @RequestMapping("/update")
    public boolean update(int id, String name, int age) {
        Student user = new Student();
        user.setId(id);
        user.setName(name);
        user.setAge(age);
        return userDao.update(user);
    }

    @RequestMapping("/query")
    public Student query(int id) {
        return userDao.getById(id);
    }

    @RequestMapping("/list")
    public List<Student> list(int start, int limit) {
        return userDao.list(start, limit);
    }
}
