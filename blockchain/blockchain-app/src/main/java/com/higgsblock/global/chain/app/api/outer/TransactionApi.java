package com.higgsblock.global.chain.app.api.outer;

import com.higgsblock.global.chain.app.api.vo.ResponseData;
import com.higgsblock.global.chain.app.blockchain.listener.MessageCenter;
import com.higgsblock.global.chain.app.blockchain.transaction.Transaction;
import com.higgsblock.global.chain.app.blockchain.transaction.UTXO;
import com.higgsblock.global.chain.app.common.constants.RespCodeEnum;
import com.higgsblock.global.chain.app.service.impl.UTXOServiceProxy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Provides the transaction sending tool class externally
 *
 * @author kongyu
 * @date 2018-03-19
 */
@RequestMapping("/v1.0.0/transactions")
@RestController
@Slf4j
public class TransactionApi {

    @Autowired
    private UTXOServiceProxy utxoServiceProxy;

    @Autowired
    private MessageCenter messageCenter;

    @RequestMapping("/send")
    public ResponseData<Boolean> sendTx(@RequestBody Transaction tx) {
        if (null == tx) {
            return ResponseData.failure(RespCodeEnum.PARAM_INVALID);
        }

        boolean result = messageCenter.dispatch(tx);
        LOGGER.info("receive transaction from browser api, hash={}", tx.getHash());
        return result ? ResponseData.success(null) : ResponseData.failure(RespCodeEnum.FAILED);
    }

    @RequestMapping("/queryUTXO")
    public ResponseData<List<UTXO>> queryUTXO(String address) {
        if (null == address) {
            return ResponseData.failure(RespCodeEnum.PARAM_INVALID);
        }

        List<UTXO> list = utxoServiceProxy.getUnionUTXO(null, address, null);
        return ResponseData.success(list);
    }
}
