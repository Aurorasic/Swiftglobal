package com.higgsblock.global.chain.app.api.outer;

import com.higgsblock.global.chain.app.api.vo.ResponseData;
import com.higgsblock.global.chain.app.common.constants.RespCodeEnum;
import com.higgsblock.global.chain.app.dao.IContractRepository;
import com.higgsblock.global.chain.app.dao.entity.ContractEntity;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * @author tangkun
 * @date 2018-10-24
 */
@RequestMapping("/v1.0.0/contract")
@RestController
@Slf4j
public class ContractApi {

    @Autowired
    private IContractRepository contractRepository;

    @RequestMapping("/get")
    public ResponseData<String> sendTxs(String key) {

        ContractEntity account = contractRepository.findOne(key);
        if (account != null) {
            return ResponseData.success(account.getValue());
        }

        return ResponseData.failure(RespCodeEnum.FAILED);
    }

    @RequestMapping("/list")
    public ResponseData<List<ContractEntity>> sendTxs() {

        List<ContractEntity> account = contractRepository.findAll();
        if (account != null) {
            return ResponseData.success(account);
        }

        return ResponseData.failure(RespCodeEnum.FAILED);
    }
}
