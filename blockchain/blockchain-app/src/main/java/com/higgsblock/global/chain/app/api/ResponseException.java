package com.higgsblock.global.chain.app.api;

import com.higgsblock.global.chain.app.constants.RespCodeEnum;
import lombok.Data;

/**
 * @author baizhengwen
 * @date 2018/3/16
 */
@Data
public class ResponseException extends RuntimeException {

    private String respCode;
    private String respMsg;

    public ResponseException(RespCodeEnum respCodeEnum) {
        this(respCodeEnum.getDesc(), respCodeEnum.getCode());
    }

    public ResponseException(String respMsg, String respCode) {
        super(respMsg);
        this.respMsg = respMsg;
        this.respCode = respCode;
    }
}
