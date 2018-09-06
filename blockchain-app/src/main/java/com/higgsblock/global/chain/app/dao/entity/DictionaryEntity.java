package com.higgsblock.global.chain.app.dao.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.keyvalue.annotation.KeySpace;

/**
 * @author baizhengwen
 * @date 2018-09-06
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@KeySpace("Dictionary")
public class DictionaryEntity {
    @Id
    private String id;
    private String value;
}
