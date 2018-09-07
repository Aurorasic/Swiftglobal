package com.higgsblock.global.chain.app.dao.entity;

import com.higgsblock.global.chain.app.keyvalue.annotation.Index;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.keyvalue.annotation.KeySpace;

/**
 * @author Su Jiulong
 * @date 2018-05-07
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@KeySpace("UTXO")
public class UTXOEntity {
    @Id
    private String id;

    @Index
    private String transactionHash;

    private short outIndex;

    private String amount;

    private String currency;

    private int scriptType;

    @Index
    private String lockScript;

    public String getId() {
        return transactionHash + outIndex;
    }
}

