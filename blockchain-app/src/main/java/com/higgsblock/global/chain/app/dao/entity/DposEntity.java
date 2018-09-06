package com.higgsblock.global.chain.app.dao.entity;

import com.higgsblock.global.chain.app.keyvalue.annotation.Index;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.keyvalue.annotation.KeySpace;

/**
 * @author yuanjiantao
 * @date 6/30/2018
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@KeySpace("DPOS")
public class DposEntity {

    @Id
    private Long id;

    @Index
    private long sn;

    private String addresses;

    public DposEntity(long sn, String addresses) {
        this.sn = sn;
        this.addresses = addresses;
    }

}
