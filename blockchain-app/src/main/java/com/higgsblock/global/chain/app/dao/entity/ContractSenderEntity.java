package com.higgsblock.global.chain.app.dao.entity;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.keyvalue.annotation.KeySpace;

/**
 * Mappings from owners of transaction inputs to sender of contract.
 *
 * @author Chen Jiawei
 * @date 2018-10-18
 */
@Data
@KeySpace("ContractSender")
public class ContractSenderEntity {
    /**
     * sender address of contract.
     */
    @Id
    private String sender;

    /**
     * owners of transaction inputs.
     */
    private String senders;
}
