package cn.primeledger.cas.global.api.outer;

import cn.primeledger.cas.global.api.service.MinerRespService;
import cn.primeledger.cas.global.api.vo.MinerBlock;
import cn.primeledger.cas.global.api.vo.ResponseData;
import cn.primeledger.cas.global.constants.RespCodeEnum;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * @author kongyu
 * @date 2018-3-20
 */
@RequestMapping("/v1.0.0/miners")
@RestController
public class MinerApi {
    @Autowired
    private MinerRespService minerRespService;

    @RequestMapping("/count")
    public ResponseData<Long> statisticsMinersNumber() {
        ResponseData<Long> responseData = null;
        long num = minerRespService.statisticsMinerNumber();
        responseData = new ResponseData<Long>(RespCodeEnum.SUCCESS, "success");
        responseData.setData(num >= 0L ? num : 0L);
        return responseData;
    }

    @RequestMapping("/check")
    public ResponseData<Boolean> isMinerByPubKey(String pubKey) {
        ResponseData<Boolean> responseData = null;
        if (null == pubKey) {
            responseData = new ResponseData<Boolean>(RespCodeEnum.PARAM_INVALID, "pubKey params is null");
            return responseData;
        }
        Boolean isMiner = minerRespService.checkIsMinerByPubKey(pubKey);
        responseData = new ResponseData<Boolean>(RespCodeEnum.SUCCESS, "success");
        responseData.setData(isMiner);
        return responseData;
    }

    @RequestMapping("/blocks")
    public ResponseData<List<MinerBlock>> statisticsMinerBlocks(String pubKey) {
        ResponseData<List<MinerBlock>> responseData = null;
        if (null == pubKey) {
            responseData = new ResponseData<List<MinerBlock>>(RespCodeEnum.PARAM_INVALID, "pubKey params is null");
            return responseData;
        }

        List<MinerBlock> minerBlocks = minerRespService.statisticsMineBlocks(pubKey);
        responseData = new ResponseData<List<MinerBlock>>(RespCodeEnum.SUCCESS, "success");
        responseData.setData(minerBlocks);
        return responseData;
    }

}
