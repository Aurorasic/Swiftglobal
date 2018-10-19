package com.higgsblock.global.chain.app.service;

import java.util.List;

/**
 * Facade of operation related to mappings, one mapping converting input senders into contract sender
 * in a contract transaction. Input senders are owners of UTXOs referenced by transaction inputs, and
 * contract sender is logic sender of contract, simply to say, is msg.sender in Solidity code, or
 * ADDRESS in EVM opcode.
 *
 * @author Chen Jiawei
 * @date 2018-10-18
 */
public interface IContractSenderService {
    /**
     * Calculates contract sender using input senders.
     *
     * @param inputSenders owners of UTXOs referenced by transaction inputs.
     * @return contract sender.
     */
    byte[] calculateContractSender(byte[]... inputSenders);

    /**
     * Gets input senders according to contract sender.
     *
     * @param contractSender contract sender.
     * @return owners of UTXOs referenced by transaction inputs.
     */
    List<byte[]> getInputSenders(byte[] contractSender);

    /**
     * Deletes mapping by contract sender.
     *
     * @param contractSender contract sender.
     * @return true if delete successfully.
     */
    boolean deleteByContractSender(byte[] contractSender);
}
