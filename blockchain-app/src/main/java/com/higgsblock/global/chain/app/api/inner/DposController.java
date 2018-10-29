package com.higgsblock.global.chain.app.api.inner;

import com.higgsblock.global.chain.app.dao.IDposRepository;
import com.higgsblock.global.chain.app.dao.entity.DposEntity;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author Chen Jiawei
 * @date 2018-10-26
 */
@RequestMapping("/Dpos")
@RestController
@Slf4j
public class DposController {
    @Autowired
    private IDposRepository dposRepository;

    @RequestMapping("/list")
    public DposEntity list(int sn) {
        return dposRepository.findBySn(sn);
    }
}
