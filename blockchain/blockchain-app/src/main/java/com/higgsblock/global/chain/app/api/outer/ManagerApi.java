package com.higgsblock.global.chain.app.api.outer;

import com.higgsblock.global.chain.app.blockchain.DataErrorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author yuanjiantao
 * @date 5/30/2018
 */
@RequestMapping("/v1.0.0/manager")
@RestController
public class ManagerApi {

    @Autowired
    private DataErrorService dataErrorService;

    @RequestMapping("/dataerror")
    public boolean dataError() {
//        dataErrorService.handleError();
        return true;
    }
}
