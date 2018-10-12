package com.higgsblock.global.chain.app.api.outer;

import com.higgsblock.global.chain.app.api.vo.ResponseData;
import com.higgsblock.global.chain.app.blockchain.DataErrorService;
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
    private DataErrorService dataErrorService;


    @RequestMapping("/reimportData")
    public ResponseData<Boolean> reimportData() {
        try {
            dataErrorService.handleError();
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
            return ResponseData.success(false);
        }
        return ResponseData.success(true);
    }
}
