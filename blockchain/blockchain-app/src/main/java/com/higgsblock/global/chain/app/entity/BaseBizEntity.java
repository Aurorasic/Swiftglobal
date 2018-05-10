package com.higgsblock.global.chain.app.entity;

import com.higgsblock.global.chain.common.entity.BaseSerializer;
import lombok.Data;

/**
 * @author baizhengwen
 * @date 2018/3/8
 */
@Data
public abstract class BaseBizEntity extends BaseSerializer {
    protected short version;

    public boolean valid(){
        if (version < 0){
            return false;
        }
        return true;
    }
}
