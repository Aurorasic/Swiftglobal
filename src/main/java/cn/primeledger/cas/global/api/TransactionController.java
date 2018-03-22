package cn.primeledger.cas.global.api;

import cn.primeledger.cas.global.api.service.TransactionRespService;
import cn.primeledger.cas.global.api.vo.ResponseData;
import cn.primeledger.cas.global.blockchain.transaction.Transaction;
import cn.primeledger.cas.global.constants.RespCodeEnum;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.List;

/**
 * @author kongyu
 * @date 2018-03-19
 */
@RequestMapping("/transactions")
@RestController
public class TransactionController {
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

    @RequestMapping("/info")
    public ResponseData<List<Transaction>> queryTxByTxHash(String hash) {
        if (null == hash) {
            return new ResponseData<List<Transaction>>(RespCodeEnum.PARAM_INVALID, "hash params is null");
        }
        List<Transaction> transactions = transactionRespService.getTransactionByTxHash(hash);
        ResponseData<List<Transaction>> responseData = new ResponseData<List<Transaction>>(RespCodeEnum.SUCCESS, "success");
        responseData.setData(transactions);
        return responseData;
    }

    @RequestMapping("/list")
    public ResponseData<List<Transaction>> queryTxByPubKey(String pubKey, String op) {
        String option = "all";
        ResponseData<List<Transaction>> responseData = null;
        if (null == pubKey) {
            responseData = new ResponseData<List<Transaction>>(RespCodeEnum.PARAM_INVALID, "pubKey params is null");
            return responseData;
        }
        option = null != op ? op : option;

        List<Transaction> transactions = transactionRespService.getTransactionByPubKey(pubKey, option);
        responseData = new ResponseData<List<Transaction>>(RespCodeEnum.SUCCESS, "success");
        responseData.setData(transactions);
        return responseData;
    }

}
