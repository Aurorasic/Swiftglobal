package cn.primeledger.cas.global.api.outer;

import cn.primeledger.cas.global.api.service.TransactionRespService;
import cn.primeledger.cas.global.api.service.UTXORespService;
import cn.primeledger.cas.global.api.vo.ResponseData;
import cn.primeledger.cas.global.blockchain.transaction.Transaction;
import cn.primeledger.cas.global.blockchain.transaction.UTXO;
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

    @RequestMapping("/info")
    public ResponseData<List<Transaction>> queryTxByTxHash(String txHash) {
        if (null == txHash) {
            return new ResponseData<List<Transaction>>(RespCodeEnum.PARAM_INVALID, "hash params is null");
        }
        List<Transaction> transactions = transactionRespService.getTransactionByTxHash(txHash);
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

        List<Transaction> transactions = transactionRespService.getTransactionByPubKeyMap(pubKey, option);
        responseData = new ResponseData<List<Transaction>>(RespCodeEnum.SUCCESS, "success");
        responseData.setData(transactions);
        return responseData;
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
