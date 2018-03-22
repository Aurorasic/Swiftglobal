package cn.primeledger.cas.global.api;

import cn.primeledger.cas.global.api.service.UTXORespService;
import cn.primeledger.cas.global.api.vo.ResponseData;
import cn.primeledger.cas.global.blockchain.transaction.UTXO;
import cn.primeledger.cas.global.constants.RespCodeEnum;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * @author kongyu
 * @date 2018-03-19
 */
@RequestMapping("/transactions")
@RestController
public class UTXOController {
    @Autowired
    private UTXORespService utxoRespService;

    @RequestMapping("/query")
    public ResponseData<List<UTXO>> query(String address) {
        if (null == address) {
            return new ResponseData<List<UTXO>>(RespCodeEnum.PARAM_INVALID, "address params is null");
        }

        List<UTXO> list = utxoRespService.getUTXOsByAddress(address);
        ResponseData<List<UTXO>> responseData = new ResponseData<List<UTXO>>(RespCodeEnum.SUCCESS, "return data");
        responseData.setData(list);
        return responseData;
    }
}
