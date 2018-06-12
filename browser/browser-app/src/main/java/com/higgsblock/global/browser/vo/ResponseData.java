package com.higgsblock.global.browser.vo;

import com.higgsblock.global.browser.enums.RespCodeEnum;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author yangshenghong
 * @date 2018-05-22
 */
@Data
@NoArgsConstructor
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

    public static <R> ResponseData<R> success(R data) {
        ResponseData<R> responseData = new ResponseData<>(RespCodeEnum.SUCCESS);
        responseData.setData(data);
        return responseData;
    }
}
