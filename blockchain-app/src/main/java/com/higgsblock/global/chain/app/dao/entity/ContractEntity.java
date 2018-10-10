package com.higgsblock.global.chain.app.dao.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.keyvalue.annotation.KeySpace;

/**
 * @author zhao xiaogang
 * @date 2018-10-10
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@KeySpace("contract")
public class ContractEntity {
    @Id
    private String key;

    private String value;
}
