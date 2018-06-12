package com.higgsblock.global.chain.app.api.outer;

import com.higgsblock.global.chain.app.api.service.TransactionRespService;
import com.higgsblock.global.chain.app.api.service.UTXORespService;
import com.higgsblock.global.chain.app.api.vo.ResponseData;
import com.higgsblock.global.chain.app.blockchain.transaction.Transaction;
import com.higgsblock.global.chain.app.blockchain.transaction.UTXO;
import com.higgsblock.global.chain.app.constants.RespCodeEnum;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * @author kongyu
 * @date 2018-03-19
 */
@RequestMapping("/v1.0.0/transactions")
@RestController
public class TransactionApi {

    @Autowired
    private UTXORespService utxoRespService;

    @Autowired
    private TransactionRespService transactionRespService;

    @RequestMapping("/send")
    public ResponseData<Boolean> sendTx(@RequestBody Transaction tx) {
        if (null == tx) {
            return new ResponseData<Boolean>(RespCodeEnum.PARAM_INVALID, "transaction params is null");
        }
        Boolean result = transactionRespService.sendTransaction(tx);
        return (result) ? new ResponseData<Boolean>(RespCodeEnum.SUCCESS, "success") : new ResponseData<Boolean>(RespCodeEnum.FAILED, "failed");
    }

    @RequestMapping("/queryUTXO")
    public ResponseData<List<UTXO>> queryUTXO(String address) {
        if (null == address) {
            return new ResponseData<List<UTXO>>(RespCodeEnum.PARAM_INVALID, "address params is null");
        }

        List<UTXO> list = utxoRespService.getUTXOsByAddress(address);
        ResponseData<List<UTXO>> responseData = new ResponseData<List<UTXO>>(RespCodeEnum.SUCCESS, "return data");
        responseData.setData(list);
        return responseData;
    }
}
