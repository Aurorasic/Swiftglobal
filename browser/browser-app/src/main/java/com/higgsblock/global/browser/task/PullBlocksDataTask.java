package com.higgsblock.global.browser.task;

import com.alibaba.fastjson.JSON;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.higgsblock.global.browser.config.AppConfig;
import com.higgsblock.global.browser.enums.RespCodeEnum;
import com.higgsblock.global.browser.enums.RewardEnum;
import com.higgsblock.global.browser.utils.HttpClient;
import com.higgsblock.global.browser.utils.UrlUtils;
import com.higgsblock.global.browser.vo.ResponseData;
import com.higgsblock.global.browser.dao.entity.*;
import com.higgsblock.global.browser.service.impl.OperatorServiceFactory;
import com.higgsblock.global.browser.utils.Money;
import com.higgsblock.global.browser.enums.SystemCurrencyEnum;
import com.higgsblock.global.chain.app.blockchain.Block;
import com.higgsblock.global.chain.app.blockchain.transaction.Transaction;
import com.higgsblock.global.chain.app.blockchain.transaction.TransactionInput;
import com.higgsblock.global.chain.app.blockchain.transaction.TransactionOutput;
import com.higgsblock.global.chain.app.schedule.BaseTask;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.SerializationUtils;

import java.sql.Timestamp;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * @author yangshenghong
 * @date 2018-05-23
 */
@Slf4j
@Component
public class PullBlocksDataTask extends BaseTask {

    /**
     * witness number
     */
    private static final int WITNESS_NUM = 11;
    /**
     * The percentage of miners' transfer fees
     */
    private static final String MINER_REWARDS_FEE = "0.8";
    /**
     * The percentage of witness' transfer fees
     */
    private static final String WITNESS_REWARDS_FEE = "0.2";
    /**
     * zero
     */
    private static final long ZERO = 0L;
    /**
     * Number of blocks per pull
     */
    private static final int LIMIT = 10;

    @Autowired
    private AppConfig appConfig;

    @Autowired
    private OperatorServiceFactory operatorServiceFactory;

    @Override
    protected void task() {
        try {
            LOGGER.info("The pull block timing task starts...");
            //ULONG destinationIP
            String ip = appConfig.getRemoteIp();
            //Destination port
            Integer port = appConfig.getRemotePort();
            //Verify IP and port, verify through operation
            if (UrlUtils.ipPortCheckout(ip, port)) {
                //build url
                String url = UrlUtils.builderUrl(ip, port, UrlUtils.GET_BLOCK);
                //Send requests for data, one at a time based on the latest local height
                String data = HttpClient.getSync(url, ImmutableMap.of("fromHeight",
                        operatorServiceFactory.getIBlockHeaderService().getMaxHeight() + 1,
                        "limit", LIMIT));
                ResponseData<List> resultData = JSON.parseObject(data, ResponseData.class);
                //The returned data is not processed correctly
                if (!RespCodeEnum.SUCCESS.getCode().equals(resultData.getRespCode())) {
                    LOGGER.warn("The pull block returns incorrectly,respCode= {},respMsg= {}",
                            resultData.getRespCode(),
                            resultData.getRespMsg());
                    return;
                }
                //Gets a collection of parsed entities and save
                getEntityDataAndSave(resultData);
                LOGGER.info("The pull block operation is completed.");
            } else {
                LOGGER.error("The ip or port format check read does not pass，ip={},port={}", ip, port);
            }
        } catch (Exception e) {
            LOGGER.error("pull block error message = {}", e.getMessage());
        }
    }

    @Override
    protected long getPeriodMs() {
        return TimeUnit.SECONDS.toMillis(15);
    }

    /**
     * The response data is parsed and assembled and save
     *
     * @param responseData
     * @return
     */
    private void getEntityDataAndSave(ResponseData<List> responseData) {
        //The response data is parsed
        for (Object o : responseData.getData()) {

            //inputMoney and outputMoney are used to calculate transaction fees,
            // transaction fees are equal to inputMoney subtraction outputMoney

            // This variable is used to count the money in the output
            // of all transactions in the current block for the cas
            Money inputMoney = new Money(ZERO);
            //This variable is used to count the money
            // in the output currency that is cas for all transactions in the current block.
            Money outputMoney = new Money(ZERO);

            Block block = JSON.parseObject(JSON.toJSONString(o), Block.class);

            if (validateBlock(block)) {
                BlockHeaderPO blockHeader = getBlockHeader(block);
                Map<String, Object> transaction = getTransactionInfo(block, inputMoney, outputMoney);
                List<RewardPO> bReward = getReward(block, inputMoney, outputMoney);
                List<MinerPO> miners = buildMinersInfo(block);
                List<String> address = getAddress2Delete(miners);
                List<UTXOPO> utxoPos = buildUTXOPO((List<TransactionOutputPO>) transaction.get("outputs"));
                operatorServiceFactory.getIBlockService().saveBlock(blockHeader, transaction, bReward, miners, address, utxoPos);
            } else {
                LOGGER.error("The pull block format is incorrect,blockHash = {}", block == null ? null : block.getHash());
                throw new RuntimeException("The block is error");
            }
        }
    }

    /**
     * build utxo
     *
     * @param transactionOutputPOS
     * @return
     */
    private List<UTXOPO> buildUTXOPO(List<TransactionOutputPO> transactionOutputPOS) {
        if (CollectionUtils.isEmpty(transactionOutputPOS)) {
            return null;
        }
        List<UTXOPO> utxoPos = Lists.newArrayList();
        transactionOutputPOS.forEach(transactionOutput -> {
            UTXOPO utxoPo = new UTXOPO();
            BeanUtils.copyProperties(transactionOutput, utxoPo);
            utxoPo.setOutIndex(transactionOutput.getIndex());
            utxoPos.add(utxoPo);
        });
        return utxoPos;
    }

    /**
     * build miner data
     *
     * @param block
     * @return
     */
    private List<MinerPO> buildMinersInfo(Block block) {
        if (null == block) {
            LOGGER.error("The blocks from which the miner's information was constructed were empty");
            throw new RuntimeException("block is null");
        }

        List<Transaction> transactions = block.getTransactions();
        if (CollectionUtils.isEmpty(transactions)) {
            LOGGER.error("block transactions is empty,blockHash = {}", block.getHash());
            throw new RuntimeException("block transactions is empty");
        }

        //This variable is used to store miner information
        Map<String, MinerPO> minersMap = Maps.newHashMap();
        String minerCurrency = SystemCurrencyEnum.MINER.getCurrency();

        //The genesis block needs to be treated separately
        if (block.isGenesisBlock()) {
            List<TransactionOutput> outputs = transactions.get(0).getOutputs();
            for (int m = 0; m < outputs.size(); m++) {
                TransactionOutput transactionOutput = outputs.get(m);
                if (null == transactionOutput) {
                    LOGGER.error("transactionOutput is empty,transactionHash = {}", transactions.get(0).getHash());
                    throw new RuntimeException("transactionOutput is empty");
                }

                String address = transactionOutput.getLockScript().getAddress();

                if (!minerCurrency.equals(transactionOutput.getMoney().getCurrency())
                        || minersMap.containsKey(address)) {
                    continue;
                }

                MinerPO bMiner3 = new MinerPO();
                bMiner3.setAmount(transactionOutput.getMoney().getValue());
                bMiner3.setAddress(address);
                minersMap.put(address, bMiner3);
            }

            return minersMap.values().stream().collect(Collectors.toList());
        }//end of for genesisBlock

        //The second block begins to count, the second transaction in the block
        for (int i = 1; i < transactions.size(); i++) {
            boolean isChargeSelf = true;

            Money moneyInputs = new Money(ZERO, minerCurrency);
            Money moneyOutputs = new Money(ZERO, minerCurrency);
            List<TransactionInput> inputs = transactions.get(i).getInputs();
            if (CollectionUtils.isEmpty(inputs)) {
                LOGGER.error("inputs is empty,transactionHash = {}", transactions.get(i).getHash());
                throw new RuntimeException("inputs is empty");
            }
            for (int n = 0; n < inputs.size(); n++) {
                TransactionInput transactionInput = inputs.get(n);
                if (null == transactionInput) {
                    LOGGER.error("transactionInput is empty,transactionHash = {}", transactions.get(i).getHash());
                    throw new RuntimeException("transactionInput is empty");
                }

                String txHash = transactionInput.getPrevOut().getHash();
                short index = transactionInput.getPrevOut().getIndex();

                TransactionOutputPO transactionOutput = operatorServiceFactory.getITransactionOutputService().getTxOutput(txHash, index);
                if (!transactionOutput.getCurrency().equals(minerCurrency)) {
                    continue;
                }

                String address = transactionOutput.getAddress();
                if (!minersMap.containsKey(address)) {
                    MinerPO bMiner1 = operatorServiceFactory.getIMinersService().getByField(address);
                    if (null == bMiner1) {
                        LOGGER.error("inputs currency miner is null,address={}", address);
                        throw new RuntimeException("inputs currency miner is null");
                    }

                    moneyInputs.add(bMiner1.getAmount());
                    minersMap.put(address, bMiner1);
                }
            }//end of for inputs

            List<TransactionOutput> outputs = transactions.get(i).getOutputs();
            if (CollectionUtils.isEmpty(outputs)) {
                LOGGER.error("outputs is empty,transactionHash = {}", transactions.get(i).getHash());
                throw new RuntimeException("outputs is empty");
            }
            for (int m = 0; m < outputs.size(); m++) {
                TransactionOutput transactionOutput = outputs.get(m);
                if (null == transactionOutput) {
                    LOGGER.error("transactionOutput is empty transactionOHash={}", transactions.get(i).getHash());
                    throw new RuntimeException("transactionOutput is empty");
                }

                String currency = transactionOutput.getMoney().getCurrency();

                if (!minerCurrency.equals(currency)) {
                    continue;
                }

                String address = transactionOutput.getLockScript().getAddress();
                String amount = transactionOutput.getMoney().getValue();
                moneyOutputs.add(amount);

                //Get the change for the forwarding address
                if (minersMap.containsKey(address)) {
                    MinerPO bMiner1 = minersMap.get(address);
                    bMiner1.setAmount(amount);
                    minersMap.put(address, bMiner1);
                    continue;
                }

                isChargeSelf = false;
                //The transfer address of the transaction
                MinerPO bMiner2 = operatorServiceFactory.getIMinersService().getByField(address);
                //If it exists, add it on the original basis
                if (null != bMiner2) {
                    Money money = new Money(bMiner2.getAmount(), minerCurrency);
                    money.add(amount);
                    bMiner2.setAmount(money.getValue());
                    minersMap.put(address, bMiner2);
                    continue;
                }

                //Addresses that have never been used in a local database
                MinerPO bMiner3 = new MinerPO();
                bMiner3.setAmount(amount);
                bMiner3.setAddress(address);
                minersMap.put(address, bMiner3);
            }//end of for outputs

            //collect address to delete
            for (MinerPO minerPO : minersMap.values()) {
                if (moneyOutputs.equals(minerPO.getAmount())) {
                    minerPO.setAmount(new Money("0").getValue());
                    minersMap.put(minerPO.getAddress(), minerPO);
                }
            }
            //check inputs and outputs
            if (1 < block.getHeight() && moneyInputs.compareTo(moneyOutputs) < 0) {
                LOGGER.error("moneyInputs < moneyOutputs");
                throw new RuntimeException("moneyInputs < moneyOutputs");
            }

            if (isChargeSelf && 1 == minersMap.size()) {
                MinerPO bMiner = minersMap.values().stream().collect(Collectors.toList()).get(0);
                bMiner.setAmount(moneyInputs.getValue());
                minersMap.put(bMiner.getAddress(), bMiner);
            }
        }
        return minersMap.values().stream().collect(Collectors.toList());
    }

    /**
     * Build a reward
     *
     * @param block
     * @param inputMoney
     * @param outputMoney
     */
    private List<RewardPO> getReward(Block block, Money inputMoney, Money outputMoney) {
        //genesis block has no reward
        if (block.isGenesisBlock()) {
            LOGGER.info("genesis block has no reward");
            return null;
        }
        //Calculate the fee in this block
        Money fee = new Money(inputMoney.getValue()).subtract(outputMoney);
        //The miners were awarded 80% commission
        Money minerFee = new Money(fee.getValue()).multiply(MINER_REWARDS_FEE);
        //The miners were awarded 20% commission
        Money witnessTotalFee = new Money(fee.getValue()).multiply(WITNESS_REWARDS_FEE);
        //The top ten witnesses received awards
        Money singleWitnessFee = new Money(witnessTotalFee.getValue()).divide(WITNESS_NUM);
        //The last witnesses received awards
        Money lastWitnessFee = witnessTotalFee.subtract(new Money(singleWitnessFee.getValue()).multiply(WITNESS_NUM - 1));

        List<RewardPO> bRewards = Lists.newArrayList();
        //only get coinBase transaction
        Transaction coinBaseTx = block.getTransactions().get(0);
        String blockHash = block.getHash();
        long blockHeight = block.getHeight();
        //if the address is miner, bReward's type will set 0 and calculate transfer fee
        // else set 1 and can not calculate transfer fee
        int witnessCode = RewardEnum.WITNESS_REWARD.getCode();
        // output size of coinBase
        int outputSize = coinBaseTx.getOutputs().size();
        //coinBase transaction outputs
        List<TransactionOutput> txOutputs = coinBaseTx.getOutputs();

        for (int i = 0; i < outputSize; i++) {
            TransactionOutput txOutput = txOutputs.get(i);
            RewardPO bReward = new RewardPO();
            bReward.setHeight(blockHeight);
            bReward.setBlockHash(blockHash);
            bReward.setAmount(txOutput.getMoney().getValue());
            bReward.setCurrency(txOutput.getMoney().getCurrency());
            bReward.setAddress(txOutput.getLockScript().getAddress());

            //The miners were awarded at the first prize
            if (i == 0) {
                bReward.setType(RewardEnum.MINER_REWARD.getCode());
                bReward.setFee(minerFee.getValue());
                bRewards.add(bReward);
                continue;
            }
            //witness's fee reward
            bReward.setType(witnessCode);
            if (i != outputSize - 1) {
                //set the last witnesses received awards
                bReward.setFee(singleWitnessFee.getValue());
            } else {
                //set the top ten witnesses received awards
                bReward.setFee(lastWitnessFee.getValue());
            }
            bRewards.add(bReward);
        }
        return bRewards;
    }

    /**
     * build blockHeader
     *
     * @param block
     */
    private BlockHeaderPO getBlockHeader(Block block) {
        BlockHeaderPO bBlockHeader = new BlockHeaderPO();
        bBlockHeader.setHeight(block.getHeight());
        bBlockHeader.setBlockHash(block.getHash());
        bBlockHeader.setBlockTime(new Timestamp(block.getBlockTime()));

        if (block.getHeight() > 1) {
            bBlockHeader.setPreBlockHash(block.getPrevBlockHash());
        }

        if (CollectionUtils.isNotEmpty(block.getMinerSelfSigPKs())) {
            bBlockHeader.setMinerAddress(block.getMinerSelfSigPKs().get(0).getAddress());
        } else {
            throw new RuntimeException("block.getMinerSelfSigPKs() is empty");
        }

        if (CollectionUtils.isNotEmpty(block.getOtherWitnessSigPKS())) {
            StringBuilder witnessAddress = new StringBuilder();
            block.getOtherWitnessSigPKS().forEach(blockWitness -> {
                witnessAddress.append(blockWitness.getAddress()).append(":");
            });
            bBlockHeader.setWitnessAddress(witnessAddress.substring(0, witnessAddress.length() - 1));
        }

        bBlockHeader.setTxNum(block.getTransactions().size());
        bBlockHeader.setBlockSize(SerializationUtils.serialize(block).length);

        return bBlockHeader;
    }

    /**
     * build transaction
     *
     * @param block
     * @param inputMoney
     * @param outputMoney
     */
    private Map<String, Object> getTransactionInfo(Block block, Money inputMoney, Money outputMoney) {
        if (CollectionUtils.isNotEmpty(block.getTransactions())) {
            Map<String, Object> transactionInfo = Maps.newHashMap();

            List<TransactionPO> bTransactions = Lists.newArrayList();
            List<TransactionInputPO> inputs = Lists.newArrayList();
            List<TransactionOutputPO> outputs = Lists.newArrayList();
            List<String> txHashIndexs = Lists.newArrayList();

            block.getTransactions().forEach(transaction -> {
                TransactionPO bTransactionBrowser = new TransactionPO();
                bTransactionBrowser.setHeight(block.getHeight());
                bTransactionBrowser.setBlockHash(block.getHash());
                bTransactionBrowser.setTransactionHash(transaction.getHash());
                bTransactionBrowser.setVersion(transaction.getVersion());
                bTransactionBrowser.setLockTime(transaction.getLockTime());
                bTransactionBrowser.setExtra(transaction.getExtra());
                bTransactions.add(bTransactionBrowser);
                //get transaction inputs
                getTransactionInput(inputs, inputMoney, txHashIndexs, transaction);
                //get transaction outputs
                getTransactionOutput(outputs, outputMoney, transaction);
            });

            //Add the result list to map
            transactionInfo.put("bTransactions", bTransactions);
            transactionInfo.put("inputs", inputs);
            transactionInfo.put("outputs", outputs);
            transactionInfo.put("txHashIndexs", txHashIndexs);
            return transactionInfo;
        }
        LOGGER.error("Return value empty = {}", block.getTransactions());
        return null;
    }

    /**
     * build transactionInput
     *
     * @param inputs
     * @param inputMoney
     * @param txHashIndexs
     * @param transaction
     */
    private void getTransactionInput(List<TransactionInputPO> inputs, Money inputMoney, List<String> txHashIndexs,
                                     Transaction transaction) {
        List<TransactionInput> transactionInputs = transaction.getInputs();
        if (CollectionUtils.isNotEmpty(transactionInputs)) {
            //The index of the current exchange on the list of transactions
            int currentIndex = 0;
            String casCurrency = SystemCurrencyEnum.CAS.getCurrency();
            StringBuilder stringBuilder = new StringBuilder();

            for (TransactionInput input : transactionInputs) {
                String preTxHash = input.getPrevOut().getHash();
                short preOutIndex = input.getPrevOut().getIndex();

                //The corresponding output according to the transaction hash and index query
                TransactionOutputPO preOutput = operatorServiceFactory.getITransactionOutputService().getTxOutput(preTxHash, preOutIndex);
                if (preOutput != null) {
                    //Add up the values of the inputs that satisfy the condition
                    if (casCurrency.equals(preOutput.getCurrency())) {
                        inputMoney.add(preOutput.getAmount());
                    }
                }

                //Collect the hash and index that you need to delete utxo
                String txHashIndex = stringBuilder.append(preTxHash).append("_").append(preOutIndex).toString();
                txHashIndexs.add(txHashIndex);
                stringBuilder.delete(0, txHashIndex.length());

                TransactionInputPO bTransactionInputBrowser = new TransactionInputPO();
                bTransactionInputBrowser.setTransactionHash(transaction.getHash());
                bTransactionInputBrowser.setIndex(currentIndex);
                bTransactionInputBrowser.setPreTransactionHash(preTxHash);
                bTransactionInputBrowser.setPreOutIndex(preOutIndex);

                StringBuilder pk = new StringBuilder();
                List<String> pkList = input.getUnLockScript().getPkList();
                if (CollectionUtils.isNotEmpty(pkList)) {
                    pkList.forEach(s -> {
                        pk.append(s).append(":");
                    });
                    bTransactionInputBrowser.setAddressList(pk.substring(0, pk.length() - 1));
                }

                inputs.add(bTransactionInputBrowser);
                currentIndex++;
            }
        }
    }

    /**
     * build transactionOutput
     *
     * @param outputs
     * @param outputMoney
     * @param transaction
     */
    private void getTransactionOutput(List<TransactionOutputPO> outputs, Money outputMoney, Transaction transaction) {
        List<TransactionOutput> transactionOutputs = transaction.getOutputs();
        if (CollectionUtils.isNotEmpty(transactionOutputs)) {
            //output index
            int index = 0;
            String casCurrency = SystemCurrencyEnum.CAS.getCurrency();

            for (TransactionOutput transactionOutput : transactionOutputs) {
                //Exclude transactions that are not input
                if (CollectionUtils.isNotEmpty(transaction.getInputs())) {
                    if (casCurrency.equals(transactionOutput.getMoney().getCurrency())) {
                        outputMoney.add(transactionOutput.getMoney().getValue());
                    }
                }
                TransactionOutputPO bTransactionOutputBrowser = new TransactionOutputPO();
                bTransactionOutputBrowser.setTransactionHash(transaction.getHash());
                bTransactionOutputBrowser.setIndex(index);
                bTransactionOutputBrowser.setAmount(new Money(transactionOutput.getMoney().getValue()).getValue());
                bTransactionOutputBrowser.setCurrency(transactionOutput.getMoney().getCurrency());
                bTransactionOutputBrowser.setScriptType(transactionOutput.getLockScript().getType());
                bTransactionOutputBrowser.setAddress(transactionOutput.getLockScript().getAddress());

                outputs.add(bTransactionOutputBrowser);
                index++;
            }
        }
    }

    /**
     * verify block data
     *
     * @param block
     * @return
     */
    private boolean validateBlock(Block block) {
        if (block == null) {
            return false;
        }
        if (block.getVersion() < 0) {
            return false;
        }
        if (block.getHeight() < 0) {
            return false;
        }
        if (block.getBlockTime() < 0) {
            return false;
        }
        if (CollectionUtils.isEmpty(block.getTransactions())) {
            return false;
        }
        if (block.getMinerSelfSigPKs().size() < 1) {
            return false;
        }
        return true;
    }

    /**
     * getting addressList to delete by miners
     *
     * @param miners
     * @return
     */
    private List<String> getAddress2Delete(List<MinerPO> miners) {
        if (CollectionUtils.isEmpty(miners)) {
            return null;
        }
        List<String> address = Lists.newArrayList();
        //Delete data if miners' value zero
        String value = new Money("0").getValue();
        for (int i = 0; i < miners.size(); i++) {
            MinerPO minerPO = miners.get(i);
            if (value.equals(minerPO.getAmount())) {
                address.add(minerPO.getAddress());
                miners.remove(i);
            }
        }
        return address;
    }
}
