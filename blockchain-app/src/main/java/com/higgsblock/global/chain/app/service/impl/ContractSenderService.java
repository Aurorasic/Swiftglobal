package com.higgsblock.global.chain.app.service.impl;

import com.alibaba.fastjson.JSON;
import com.google.common.base.Charsets;
import com.google.common.collect.Lists;
import com.google.common.hash.HashFunction;
import com.google.common.hash.Hashing;
import com.higgsblock.global.chain.app.dao.IContractSenderRepository;
import com.higgsblock.global.chain.app.dao.entity.ContractSenderEntity;
import com.higgsblock.global.chain.app.service.IContractSenderService;
import lombok.extern.slf4j.Slf4j;
import org.spongycastle.util.encoders.Hex;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Manages mappings between input senders and contract sender in contract transactions.
 *
 * @author Chen Jiawei
 * @date 2018-10-18
 */
@Service
@Slf4j
public class ContractSenderService implements IContractSenderService {
    /**
     * Maximum of allowed input senders in a transaction.
     */
    private static final int SENDER_NUMBER_LIMITATION = 64;

    /**
     * Number of bytes storing address.
     */
    private static final int ADDRESS_BYTES_NUMBER = 20;

    /**
     * Maximum of allowed tries in the procedure of generating a unique contract sender.
     */
    private static final int TRYING_TIMES_LIMITATION = 10;

    @Autowired
    private IContractSenderRepository contractSenderRepository;


    @Override
    public byte[] calculateContractSender(byte[]... inputSenders) {
        if (inputSenders.length > SENDER_NUMBER_LIMITATION) {
            throw new IllegalArgumentException("input senders of contract exceed the number limit.");
        }

        for (byte[] inputSender : inputSenders) {
            if (inputSender == null) {
                throw new IllegalArgumentException("some input sender is null.");
            }

            if (inputSender.length != ADDRESS_BYTES_NUMBER) {
                throw new IllegalArgumentException(
                        String.format("address of some sender is not of %d bytes.", ADDRESS_BYTES_NUMBER));
            }
        }

        byte[] contractSenderBase = shuffle(inputSenders);
        byte[] contractSender = extractUniqueContractSender(contractSenderBase);
        if (contractSender == null) {
            throw new IllegalArgumentException(
                    String.format("Can not calculate a unique address after %d tries.", TRYING_TIMES_LIMITATION));
        }

        contractSenderRepository.save(
                DataConverter.convertToContractSenderEntity(contractSender, Lists.newArrayList(inputSenders)));
        return contractSender;
    }

    /**
     * Shuffles specific bytes list.
     *
     * @param items bytes list, each cannot be null, and must be 20-bytes.
     * @return shuffling output.
     */
    private byte[] shuffle(byte[]... items) {
        HashFunction function = Hashing.sha256();

        StringBuilder builder = new StringBuilder();
        for (byte[] item : items) {
            builder.append(function.hashBytes(item));
        }

        return function.hashString(builder.toString(), Charsets.UTF_8).asBytes();
    }

    /**
     * Extracts contract sender from specific data.
     *
     * @param contractSenderBase data from which contract sender is extracted.
     * @return contract sender, null if extracting fails.
     */
    private byte[] extractUniqueContractSender(byte[] contractSenderBase) {
        byte[] contractSender;

        for (int i = 0; i < TRYING_TIMES_LIMITATION; i++) {
            contractSender = new byte[ADDRESS_BYTES_NUMBER];
            System.arraycopy(contractSenderBase, i, contractSender, 0, ADDRESS_BYTES_NUMBER);

            List<ContractSenderEntity> mappings =
                    contractSenderRepository.findBySender(DataConverter.convertToHexString(contractSender));
            if (mappings.size() == 0) {
                return contractSender;
            }
        }

        return null;
    }

    /**
     * Converter responsible for data converting between memory and db.
     */
    private static class DataConverter {
        private static String convertToHexString(byte[] bytes) {
            return Hex.toHexString(bytes);
        }

        static String convertToJsonString(List<byte[]> bytesList) {
            return JSON.toJSONString(bytesList);
        }

        static List<byte[]> convertToBytesList(String bytesList) {
            return JSON.parseArray(bytesList, byte[].class);
        }

        static ContractSenderEntity convertToContractSenderEntity(byte[] contractSender, List<byte[]> inputSenderList) {
            ContractSenderEntity contractSenderEntity = new ContractSenderEntity();

            contractSenderEntity.setSender(convertToHexString(contractSender));
            contractSenderEntity.setSenders(convertToJsonString(inputSenderList));

            return contractSenderEntity;
        }
    }

    @Override
    public List<byte[]> getInputSenders(byte[] contractSender) {
        List<ContractSenderEntity> mappings =
                contractSenderRepository.findBySender(DataConverter.convertToHexString(contractSender));
        ContractSenderEntity mapping = mappings.get(0);

        if (mapping == null) {
            return null;
        }

        return DataConverter.convertToBytesList(mapping.getSenders());
    }

    @Override
    public boolean deleteByContractSender(byte[] contractSender) {
        return contractSenderRepository.deleteBySender(DataConverter.convertToHexString(contractSender)) > 0;
    }
}
