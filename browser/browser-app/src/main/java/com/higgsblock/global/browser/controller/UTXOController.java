package com.higgsblock.global.browser.controller;

import com.google.common.collect.Lists;
import com.higgsblock.global.browser.enums.RespCodeEnum;
import com.higgsblock.global.browser.vo.ResponseData;
import com.higgsblock.global.browser.vo.UTXOVO;
import com.higgsblock.global.browser.dao.entity.UTXOPO;
import com.higgsblock.global.browser.service.iface.IUTXOService;
import com.higgsblock.global.chain.crypto.ECKey;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * @author Su Jiulong
 * @date 2018-05-29
 */
@RequestMapping("/v1.0.0/utxos/")
@RestController
@Slf4j
public class UTXOController {

    @Autowired
    private IUTXOService iutxoService;

    @RequestMapping(value = "utxo", method = RequestMethod.GET)
    public ResponseData<List<UTXOVO>> getUTXOsByAddress(String address) {
        //Verify that the address is correct
        if (!ECKey.checkBase58Addr(address)) {
            LOGGER.error("The address not verified  by ECKey  address = {}", address);
            return new ResponseData<List<UTXOVO>>(RespCodeEnum.PARAMETER_ERROR, "The address not verified  by ECKey");
        }

        //get UTXOPoList by address
        List<UTXOPO> utxoPos = iutxoService.getUTXOsByAddress(address);
        if (CollectionUtils.isEmpty(utxoPos)) {
            return new ResponseData<List<UTXOVO>>(RespCodeEnum.DATA_ERROR, "The address has not utxo  address");
        }

        //UTXOPoList convert to UTXOVoList
        List<UTXOVO> utxoVos = Lists.newArrayList();
        utxoPos.forEach(utxoPo -> {
            UTXOVO utxoVo = new UTXOVO();
            BeanUtils.copyProperties(utxoPo, utxoVo);
            utxoVos.add(utxoVo);
        });

        return ResponseData.success(utxoVos);
    }
}
