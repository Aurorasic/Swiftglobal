package com.higgsblock.global.chain.app.api.outer;

import com.higgsblock.global.chain.app.api.vo.ResponseData;
import com.higgsblock.global.chain.app.blockchain.DataErrorProcessor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * The browser accesses the whitelist filter
 *
 * @author kongyu
 * @date 2018-5-21 16:20
 */
@RequestMapping("/v1.0.0/manager")
@RestController
@Slf4j
public class ManagerApi {

    @Autowired
    private DataErrorProcessor dataErrorProcessor;


    @RequestMapping("/reimportData")
    public ResponseData<Boolean> reimportData() {
        dataErrorProcessor.handleError();
        return ResponseData.success(true);
    }
}
