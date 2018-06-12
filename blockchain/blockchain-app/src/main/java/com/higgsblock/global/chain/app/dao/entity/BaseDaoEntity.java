package com.higgsblock.global.chain.app.dao.entity;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * @author Zhao xiaogang
 * @date 2018-05-22
 */
@Data
@AllArgsConstructor
public class BaseDaoEntity {
    Object key;
    Object value;
    String columnFamilyName;
}
