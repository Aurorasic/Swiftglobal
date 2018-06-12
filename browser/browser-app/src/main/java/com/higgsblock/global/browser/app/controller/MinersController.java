package com.higgsblock.global.browser.app.controller;

import com.google.common.collect.Maps;
import com.higgsblock.global.browser.app.constants.RespCodeEnum;
import com.higgsblock.global.browser.app.vo.PageRewardBlockVO;
import com.higgsblock.global.browser.app.vo.ResponseData;
import com.higgsblock.global.browser.service.bo.PageEntityBO;
import com.higgsblock.global.browser.service.bo.RewardBlockBO;
import com.higgsblock.global.browser.service.iface.IMinersService;
import com.higgsblock.global.browser.service.iface.IRewardService;
import com.higgsblock.global.chain.crypto.ECKey;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * @author Su Jiulong
 * @date 2018-05-21
 */
@RequestMapping("/v1.0.0/miners/")
@RestController
@Slf4j
public class MinersController {

    @Autowired
    private IMinersService iMinersService;

    @Autowired
    private IRewardService iRewardService;

    @RequestMapping(value = "count", method = RequestMethod.GET)
    public ResponseData<Map> getMinersCount() {
        long minersCount = iMinersService.getMinersCount();
        Map<String, Long> countMap = Maps.newHashMap();
        countMap.put("count", minersCount);
        return ResponseData.success(countMap);
    }

    @RequestMapping(value = "isMiner", method = RequestMethod.GET)
    public ResponseData<Map> isMiner(String pubKey) {
        if (StringUtils.isEmpty(pubKey)) {
            return new ResponseData<Map>(RespCodeEnum.PARAMETER_ERROR, "pubKey is empty");
        }

        String address = null;
        try {
            address = ECKey.pubKey2Base58Address(pubKey);
        } catch (Exception e) {
            LOGGER.error("pubKey2Base58Address error pubKey={}", pubKey);
            return new ResponseData<Map>(RespCodeEnum.PARAMETER_ERROR, "Public key invalid");
        }

        Boolean result = iMinersService.isMiner(address);
        Map<String, Boolean> isMinerVoMap = Maps.newHashMap();
        isMinerVoMap.put("stat", result);
        return ResponseData.success(isMinerVoMap);
    }

    @RequestMapping(value = "pageBlocks", method = RequestMethod.GET)
    public ResponseData<PageRewardBlockVO> getRewardBlocksByPage(String pubKey, Integer start, Integer limit) {
        if (StringUtils.isEmpty(pubKey)) {
            return new ResponseData<PageRewardBlockVO>(RespCodeEnum.PARAMETER_ERROR, "pubKey is empty");
        }

        //Give them default values if they start or are limited to null or equal to 0
        if (start == null || start <= 0) {
            start = 1;
        }
        if (limit == null || limit <= 0) {
            limit = 10;
        }

        String address = null;
        try {
            address = ECKey.pubKey2Base58Address(pubKey);
        } catch (Exception e) {
            LOGGER.error("pubKey2Base58Address error pubKey={}", pubKey);
            return new ResponseData<PageRewardBlockVO>(RespCodeEnum.PARAMETER_ERROR, "Public key invalid");
        }
        //Pages to find
        PageEntityBO<RewardBlockBO> totalRewardBlockBo = iRewardService.findByPage(address, start, limit);
        if (totalRewardBlockBo == null) {
            return new ResponseData<PageRewardBlockVO>(RespCodeEnum.DATA_ERROR, "The request data does not exist");
        }

        //totalRewardBlockBo convert to pageRewardBlockVo
        PageRewardBlockVO pageRewardBlockVo = new PageRewardBlockVO();
        BeanUtils.copyProperties(totalRewardBlockBo, pageRewardBlockVo);
        pageRewardBlockVo.setCountTotal(totalRewardBlockBo.getTotal());

        return ResponseData.success(pageRewardBlockVo);
    }

    @RequestMapping(value = "blocks", method = RequestMethod.GET)
    public ResponseData<PageRewardBlockVO> getRewardBlocks(String pubKey) {
        if (StringUtils.isEmpty(pubKey)) {
            return new ResponseData<PageRewardBlockVO>(RespCodeEnum.PARAMETER_ERROR, "pubKey is empty");
        }

        String address = null;
        try {
            address = ECKey.pubKey2Base58Address(pubKey);
        } catch (Exception e) {
            LOGGER.error("pubKey2Base58Address error pubKey={}", pubKey);
            return new ResponseData<PageRewardBlockVO>(RespCodeEnum.PARAMETER_ERROR, "Public key invalid");
        }
        //Pages to find
        PageEntityBO<RewardBlockBO> totalRewardBlockBo = iRewardService.getRewardBlocks(address);
        if (totalRewardBlockBo == null) {
            return new ResponseData<PageRewardBlockVO>(RespCodeEnum.DATA_ERROR, "The request data does not exist");
        }

        //totalRewardBlockBo convert to pageRewardBlockVo
        PageRewardBlockVO pageRewardBlockVo = new PageRewardBlockVO();
        BeanUtils.copyProperties(totalRewardBlockBo, pageRewardBlockVo);
        pageRewardBlockVo.setCountTotal(totalRewardBlockBo.getTotal());

        return ResponseData.success(pageRewardBlockVo);
    }
}
