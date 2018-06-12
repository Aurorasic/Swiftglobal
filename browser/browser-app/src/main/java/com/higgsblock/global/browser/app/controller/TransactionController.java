package com.higgsblock.global.browser.app.controller;

import com.alibaba.fastjson.JSON;
import com.google.common.collect.Sets;
import com.higgsblock.global.browser.app.config.AppConfig;
import com.higgsblock.global.browser.app.constants.OpEnum;
import com.higgsblock.global.browser.app.constants.RespCodeEnum;
import com.higgsblock.global.browser.app.utils.HttpClient;
import com.higgsblock.global.browser.app.utils.UrlUtils;
import com.higgsblock.global.browser.app.vo.ResponseData;
import com.higgsblock.global.browser.app.vo.TransactionItemsVO;
import com.higgsblock.global.browser.app.vo.TransactionVO;
import com.higgsblock.global.browser.service.bo.TransactionBO;
import com.higgsblock.global.browser.service.bo.TransactionItemsBO;
import com.higgsblock.global.browser.service.iface.ITransactionInputService;
import com.higgsblock.global.browser.service.iface.ITransactionOutputService;
import com.higgsblock.global.browser.service.iface.ITransactionService;
import com.higgsblock.global.chain.app.blockchain.transaction.Transaction;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.Set;


/**
 * @author yangshenghong
 * @date 2018-05-21
 */
@RequestMapping("/v1.0.0/transactions/")
@RestController
@Slf4j
public class TransactionController {

    @Autowired
    private ITransactionService iTransactionService;
    @Autowired
    private ITransactionInputService iTransactionInputService;
    @Autowired
    private ITransactionOutputService iTransactionOutputService;

    @Autowired
    private AppConfig appConfig;

    @RequestMapping(value = "send", method = RequestMethod.POST)
    public ResponseData<Boolean> sendTransaction(@RequestBody Transaction transaction) {
        if (transaction == null || !transaction.valid()) {
            return new ResponseData<Boolean>(RespCodeEnum.PARAMETER_ERROR, "BTransaction check failure");
        }

        String ip = appConfig.getRemoteIp();
        Integer port = appConfig.getRemotePort();

        if (UrlUtils.ipPortCheckout(ip, port)) {
            String url = UrlUtils.builderUrl(ip, port, UrlUtils.SEND_TRANSACTION);
            String result = HttpClient.doPost(url, transaction);
            return JSON.parseObject(result, ResponseData.class);
        }

        return new ResponseData<Boolean>(RespCodeEnum.PARAMETER_ERROR, "ip or port error!!");
    }

    @RequestMapping(value = "info", method = RequestMethod.GET)
    public ResponseData<TransactionVO> getTransactionByHash(String hash) {
        if (StringUtils.isEmpty(hash)) {
            return new ResponseData<TransactionVO>(RespCodeEnum.PARAMETER_ERROR, "TransactionHash check failure");
        }

        TransactionBO transactionBo = iTransactionService.getTransactionByHash(hash);
        if (null == transactionBo) {
            return new ResponseData<>(RespCodeEnum.DATA_ERROR, "This data is not found in the database.");
        }

        TransactionVO transactionVo = new TransactionVO();
        BeanUtils.copyProperties(transactionBo, transactionVo);

        return ResponseData.success(transactionVo);
    }

    @RequestMapping(value = "list", method = RequestMethod.GET)
    public ResponseData getTransactionInfoByPubKey(String pubKey, String op) {
        if (StringUtils.isEmpty(pubKey)) {
            return new ResponseData<TransactionItemsVO>(RespCodeEnum.PARAMETER_ERROR, "pubKey is empty");
        }

        if (StringUtils.isEmpty(op)) {
            op = OpEnum.ALL.getType();
        }

        Set<String> opSet = Sets.newTreeSet();
        opSet.add(OpEnum.ALL.getType());
        opSet.add(OpEnum.IN.getType());
        opSet.add(OpEnum.OUT.getType());

        if (!opSet.contains(op)) {
            return new ResponseData(RespCodeEnum.PARAMETER_ERROR, "The parameter is not in the given range.");
        }

        TransactionItemsBO transactionItemsBo = null;

        switch (op) {
            case "in":
                transactionItemsBo = iTransactionInputService.getTxInputBosByPubKey(pubKey);
                break;
            case "out":
                transactionItemsBo = iTransactionOutputService.getTxOutputBosByPubKey(pubKey);
                break;
            default:
                transactionItemsBo = iTransactionService.getTransactionByPk(pubKey);
                break;
        }

        if (transactionItemsBo == null) {
            return new ResponseData<TransactionItemsVO>(RespCodeEnum.DATA_ERROR,
                    "This data is not found in the database.");
        }

        TransactionItemsVO transactionItemsVo = new TransactionItemsVO();
        BeanUtils.copyProperties(transactionItemsBo, transactionItemsVo);

        return ResponseData.success(transactionItemsVo);
    }
}
