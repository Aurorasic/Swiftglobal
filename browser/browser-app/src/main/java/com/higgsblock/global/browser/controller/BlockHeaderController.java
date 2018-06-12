package com.higgsblock.global.browser.controller;

import com.higgsblock.global.browser.enums.RespCodeEnum;
import com.higgsblock.global.browser.vo.BlockHeaderVO;
import com.higgsblock.global.browser.vo.BlockVO;
import com.higgsblock.global.browser.vo.PageResultVO;
import com.higgsblock.global.browser.vo.ResponseData;
import com.higgsblock.global.browser.service.bo.BlockBO;
import com.higgsblock.global.browser.service.bo.BlockHeaderBO;
import com.higgsblock.global.browser.service.bo.PageEntityBO;
import com.higgsblock.global.browser.service.iface.IBlockHeaderService;
import com.higgsblock.global.browser.service.iface.IBlockService;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

/**
 * @author yangshenghong
 * @date 2018-05-22
 */
@RequestMapping("/v1.0.0/blocks/")
@RestController
public class BlockHeaderController {

    @Autowired
    private IBlockHeaderService iBlockHeaderService;

    @Autowired
    private IBlockService iBlockService;

    @RequestMapping(value = "info", method = RequestMethod.GET)
    public ResponseData<BlockVO> getBlockByHash(String hash) {
        if (StringUtils.isEmpty(hash)) {
            return new ResponseData<BlockVO>(RespCodeEnum.PARAMETER_ERROR, "BlockHash check failure!!");
        }

        BlockBO blockBo = iBlockService.getBlockByHash(hash);
        if (blockBo == null) {
            return new ResponseData<BlockVO>(RespCodeEnum.DATA_ERROR, "This data is bot found in the database!!");
        }

        //Convert the result to the corresponding VO return
        BlockVO blockVo = new BlockVO();
        BeanUtils.copyProperties(blockBo, blockVo);
        return ResponseData.success(blockVo);
    }

    @RequestMapping(value = "header", method = RequestMethod.GET)
    public ResponseData<BlockHeaderVO> getBlockHeaderByHash(String hash) {
        if (StringUtils.isEmpty(hash)) {
            return new ResponseData<BlockHeaderVO>(RespCodeEnum.PARAMETER_ERROR, "BlockHash check failure!!");
        }

        BlockHeaderBO blockHeaderBo = iBlockHeaderService.getByField(hash);
        if (blockHeaderBo == null) {
            return new ResponseData<BlockHeaderVO>(RespCodeEnum.DATA_ERROR, "This data is bot found in the database!!");
        }

        //Convert the result to the corresponding VO return
        BlockHeaderVO blockHeaderVo = new BlockHeaderVO();
        BeanUtils.copyProperties(blockHeaderBo, blockHeaderVo);
        return ResponseData.success(blockHeaderVo);
    }

    @RequestMapping(value = "headerList", method = RequestMethod.GET)
    public ResponseData<PageResultVO<BlockHeaderVO>> getBlockHeaderList(Long start, Long limit) {
        //Give them default values if they start or are limited to null or equal to 0
        if (start == null || start <= 0) {
            start = 1L;
        }

        if (limit == null || limit <= 0) {
            limit = 50L;
        }

        PageEntityBO<BlockHeaderBO> pageEntityBo = iBlockHeaderService.findScopeBlock(start, limit);
        if (pageEntityBo == null || CollectionUtils.isEmpty(pageEntityBo.getItems())) {
            return new ResponseData<PageResultVO<BlockHeaderVO>>(RespCodeEnum.DATA_ERROR, "No data is returned!!");
        }

        //Convert the result to the corresponding VO return
        List<BlockHeaderVO> blockHeaderVos = new ArrayList<>();
        pageEntityBo.getItems().forEach((blockHeaderBo -> {
            BlockHeaderVO blockHeaderVo = new BlockHeaderVO();
            BeanUtils.copyProperties(blockHeaderBo, blockHeaderVo);
            blockHeaderVos.add(blockHeaderVo);
        }));

        return ResponseData.success(PageResultVO.createBuilder()
                .withItmes(blockHeaderVos)
                .withTotal(pageEntityBo.getTotal())
                .builder());
    }

    @RequestMapping(value = "recentHeaderList", method = RequestMethod.GET)
    public ResponseData<PageResultVO<BlockHeaderVO>> recentHeaderList(Long limit) {
        //give them default values if limit is null or equals 0
        if (limit == null || limit <= 0) {
            limit = 50L;
        }

        List<BlockHeaderBO> latestBlock = iBlockHeaderService.getLatestBlock(limit);
        if (CollectionUtils.isEmpty(latestBlock)) {
            return new ResponseData<PageResultVO<BlockHeaderVO>>(RespCodeEnum.DATA_ERROR, "This data is bot found in the database!!");
        }

        //Convert the result to the corresponding VO return
        List<BlockHeaderVO> blockHeaderVos = new ArrayList<>();
        latestBlock.forEach((blockHeaderBo -> {
            BlockHeaderVO blockHeaderVo = new BlockHeaderVO();
            BeanUtils.copyProperties(blockHeaderBo, blockHeaderVo);
            blockHeaderVos.add(blockHeaderVo);
        }));

        return ResponseData.success(PageResultVO.createBuilder()
                .withTotal(blockHeaderVos.size())
                .withItmes(blockHeaderVos)
                .builder());
    }
}
