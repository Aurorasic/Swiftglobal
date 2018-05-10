package com.higgsblock.global.chain.app.api.vo;

import com.higgsblock.global.chain.app.constants.RespCodeEnum;
import lombok.Data;

/**
 * @author baizhengwen
 * @date 2018/3/16
 */
@Data
public class ResponseData<T> {
    private String respCode;
    private String respMsg;
    private T data;

    public ResponseData(RespCodeEnum respCodeEnum) {
        this(respCodeEnum.getCode(), respCodeEnum.getDesc());
    }

    public ResponseData(RespCodeEnum respCodeEnum, String respMsg) {
        this(respCodeEnum.getCode(), respMsg);
    }

    public ResponseData(String respCode, String respMsg) {
        this.respCode = respCode;
        this.respMsg = respMsg;
    }

    public static <R> ResponseData<R> failure(RespCodeEnum respCodeEnum) {
        return new ResponseData<>(respCodeEnum);
    }

    public static <R> ResponseData<R> failure(String respCode, String respMsg) {
        return new ResponseData<>(respCode, respMsg);
    }

    public static <R> ResponseData<R> success(R data) {
        ResponseData<R> responseData = new ResponseData<>(RespCodeEnum.SUCCESS);
        responseData.setData(data);
        return responseData;
    }

    public RespCodeEnum respCodeEnum() {
        return RespCodeEnum.getByCode(respCode);
    }
}
