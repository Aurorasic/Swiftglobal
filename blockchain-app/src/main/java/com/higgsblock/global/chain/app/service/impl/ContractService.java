package com.higgsblock.global.chain.app.service.impl;

import com.google.common.base.Charsets;
import com.google.common.hash.HashFunction;
import com.google.common.hash.Hashing;
import com.higgsblock.global.chain.app.blockchain.Block;
import com.higgsblock.global.chain.app.blockchain.script.LockScript;
import com.higgsblock.global.chain.app.blockchain.transaction.*;
import com.higgsblock.global.chain.app.contract.BalanceUtil;
import com.higgsblock.global.chain.app.contract.ContractTransaction;
import com.higgsblock.global.chain.app.contract.Helpers;
import com.higgsblock.global.chain.app.service.IContractService;
import com.higgsblock.global.chain.app.utils.AddrUtil;
import com.higgsblock.global.chain.common.enums.SystemCurrencyEnum;
import com.higgsblock.global.chain.common.utils.Money;
import com.higgsblock.global.chain.vm.api.ExecutionEnvironment;
import com.higgsblock.global.chain.vm.api.ExecutionResult;
import com.higgsblock.global.chain.vm.api.Executor;
import com.higgsblock.global.chain.vm.config.BlockchainConfig;
import com.higgsblock.global.chain.vm.config.ByzantiumConfig;
import com.higgsblock.global.chain.vm.config.DefaultSystemProperties;
import com.higgsblock.global.chain.vm.core.Repository;
import com.higgsblock.global.chain.vm.core.SystemProperties;
import com.higgsblock.global.chain.vm.fee.CurrencyUnitEnum;
import com.higgsblock.global.chain.vm.fee.FeeUtil;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.spongycastle.util.encoders.Hex;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.List;

/**
 * @author tangkun
 * @date 2018-10-10
 */
@Slf4j
public class ContractService implements IContractService {

    @Autowired
    private ByzantiumConfig blockchainConfig;

    @Autowired
    private UTXOServiceProxy utxoServiceProxy;

    @Autowired
    private DefaultSystemProperties systemProperties;

    @Setter
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class InvokePO {

        private ExecutionResult executionResult;

        private String stateHash;

        private ContractTransaction contractTransaction;
    }

    @Override
    public InvokePO invoke(Block block, Transaction transaction, Repository blockRepository, int totalUsedSize, long totalUsedGas, Money totalFee) {
        {
            if (totalUsedSize + transaction.getSize() > blockchainConfig.getLimitedSize()) {
                return null;
            }

            if (!transaction.isContractTrasaction()) {

                totalUsedSize += transaction.getSize();
                totalFee = totalFee.add(BalanceUtil.convertGasToMoney(
                        FeeUtil.getSizeGas(transaction.getSize()).multiply(transaction.getGasPrice()), SystemCurrencyEnum.CAS.getCurrency()));
                totalUsedGas += FeeUtil.getSizeGas(transaction.getSize()).longValue();
            } else {

                if (totalUsedSize + transaction.getSize() + blockchainConfig.getContractLimitedSize() > blockchainConfig.getLimitedSize()) {
                    return null;
                }

                totalUsedSize += transaction.getSize();
                Repository transactionRepository = blockRepository.startTracking();


                ExecutionResult executionResult = executeContract(transaction, block, transactionRepository);
                InvokePO invokePO = new InvokePO();

                //TODO tangKun refund gas 2018-09-29
                boolean success = StringUtils.isEmpty(executionResult.getErrorMessage());
                if (!success) {
                    Money transferMoney = transaction.getOutputs().get(0).getMoney() == null ?
                            new Money(BigDecimal.ZERO.toPlainString()) : transaction.getOutputs().get(0).getMoney();

                    if (transferMoney.compareTo(new Money(BigDecimal.ZERO.toPlainString())) > 0) {

                        ContractTransaction refundTx = new ContractTransaction();
                        TransactionInput input = new TransactionInput();
                        TransactionOutPoint top = new TransactionOutPoint();
                        top.setTransactionHash(transaction.getHash());
                        top.setIndex((short) 0);

                        input.setPrevOut(top);
                        refundTx.getInputs().add(input);

                        TransactionOutput out = new TransactionOutput();
                        out.setMoney(transferMoney);
                        LockScript lockScript = new LockScript();

                        lockScript.setAddress(AddrUtil.toTransactionAddr(getSender(transaction, block)));
                        lockScript.setType(ScriptTypeEnum.P2PK.getType());
                        out.setLockScript(lockScript);
                        refundTx.getOutputs().add(out);

                        refundTx.setVersion(transaction.getVersion());
                        refundTx.setLockTime(transaction.getLockTime());
                        refundTx.setTransactionTime(System.currentTimeMillis());

                        invokePO.setContractTransaction(refundTx);
                        totalUsedSize += transaction.getSize();
                    }
                    totalFee = totalFee.add(BalanceUtil.convertGasToMoney(BigInteger.valueOf(transaction.getGasLimit()).multiply(transaction.getGasPrice())
                            , SystemCurrencyEnum.CAS.getCurrency()));
                    totalUsedGas += transaction.getGasLimit();

                } else {
                    boolean transferFlag = transactionRepository.getAccountDetails().size() > 0 || executionResult.getGasRefund().compareTo(BigInteger.ZERO) > 0;
                    if (transferFlag) {
                        List<UTXO> unSpendAsset = transactionRepository.getUnSpendAsset(transaction.getContractAddress());
                        ContractTransaction contractTx = Helpers.buildContractTransaction(unSpendAsset,
                                transactionRepository.getAccountState(transaction.calculateContractAddress(), SystemCurrencyEnum.CAS.getCurrency()),
                                transactionRepository.getAccountDetails());
                        invokePO.setContractTransaction(contractTx);

                        totalUsedSize += transaction.getSize();
                        totalFee = totalFee.add(BalanceUtil.convertGasToMoney(executionResult.getGasUsed().multiply(transaction.getGasPrice())
                                , SystemCurrencyEnum.CAS.getCurrency()));
                        totalUsedGas += executionResult.getGasUsed().longValue();
                    }

                }
                invokePO.setExecutionResult(executionResult);
                invokePO.setStateHash(calculateExecutionHash(executionResult));
                transactionRepository.commit();

                return invokePO;
            }
            return null;
        }
    }

    /**
     * Executes contract.
     *
     * @param transaction  original transaction containing the contract.
     * @param block        current block.
     * @param txRepository snapshot of db before the transaction is executed.
     * @return a result recorder of the contract execution. null indicates that this transaction cannot be packaged into the block.
     */
    private ExecutionResult executeContract(Transaction transaction,
                                            Block block, Repository txRepository) {
        ExecutionEnvironment executionEnvironment = createExecutionEnvironment(transaction, block, systemProperties, blockchainConfig);
        Executor executor = new Executor(txRepository, executionEnvironment);
        ExecutionResult executionResult = executor.execute();

        LOGGER.info(executionResult.toString());
        return executionResult;
    }

    /**
     * Creates an environment in which the contract can be executed.
     *
     * @param transaction      original transaction containing a contract.
     * @param block            block original transaction is to be packaged into.
     * @param systemProperties configuration of system behaviour.
     * @param blockchainConfig configuration of current block.
     * @return environment for contract execution.
     */
    private ExecutionEnvironment createExecutionEnvironment(
            Transaction transaction, Block block, SystemProperties systemProperties, BlockchainConfig blockchainConfig) {
        ExecutionEnvironment executionEnvironment = new ExecutionEnvironment();

        // sets transaction context.
        executionEnvironment.setTransactionHash(transaction.getHash());
        executionEnvironment.setContractCreation(transaction.isContractCreation());
        executionEnvironment.setContractAddress(transaction.getContractAddress());
        executionEnvironment.setSenderAddress(getSender(transaction, block));
        executionEnvironment.setGasPrice(transaction.getGasPrice().toByteArray());
        executionEnvironment.setGasLimit(BigInteger.valueOf(transaction.getGasLimit()).toByteArray());
        executionEnvironment.setValue(new BigDecimal(transaction.getOutputs().get(0).getMoney().getValue())
                .multiply(new BigDecimal(CurrencyUnitEnum.CAS.getWeight())).toBigInteger().toByteArray());
        executionEnvironment.setData(transaction.getContractParameters().getBytecode());
        executionEnvironment.setSizeGas(FeeUtil.getSizeGas(transaction.getSize()).longValue());

        // sets block context.
        // difficulty is meaningless in the case of dpos consensus. we can set it zero to indicates that there is no random calculation.
        executionEnvironment.setParentHash(Hex.decode(block.getPrevBlockHash()));
        executionEnvironment.setCoinbase(AddrUtil.toContractAddr(block.getMinerSigPair().getAddress()));
        executionEnvironment.setTimestamp(block.getBlockTime());
        executionEnvironment.setNumber(block.getHeight());
        executionEnvironment.setDifficulty(BigInteger.valueOf(0L).toByteArray());
        executionEnvironment.setGasLimitBlock(BigInteger.valueOf(Block.LIMITED_GAS).toByteArray());
        executionEnvironment.setBalance(BigInteger.valueOf(getBalance(transaction.getContractAddress(), block)).toByteArray());

        // sets system behaviour.
        executionEnvironment.setSystemProperties(systemProperties);

        // sets configuration of current block.
        executionEnvironment.setBlockchainConfig(blockchainConfig);

        return executionEnvironment;
    }

    /**
     * Gets sender of the contract in specific transaction.
     *
     * @param transaction transaction containing the target contract.
     * @param block       current block.
     * @return sender of contract.
     */
    private byte[] getSender(Transaction transaction, Block block) {
        return AddrUtil.toContractAddr(getPreOutput(block.getPrevBlockHash(),
                transaction.getInputs().get(0)).getLockScript().getAddress());
    }

    /**
     * Gets balance of specific address.
     *
     * @param address target address.
     * @param block   current block.
     * @return balance of the address.
     */
    private long getBalance(byte[] address, Block block) {
        List<UTXO> utxoList = utxoServiceProxy.getUnionUTXO(
                block.getPrevBlockHash(), AddrUtil.toTransactionAddr(address), SystemCurrencyEnum.CAS.getCurrency());

        Money balance = new Money(0L);
        for (UTXO utxo : utxoList) {
            balance.add(utxo.getOutput().getMoney());
        }

        return new BigDecimal(balance.getValue()).multiply(new BigDecimal(CurrencyUnitEnum.CAS.getWeight())).longValue();
    }

    /**
     * Calculates hash of result related to contract execution procedure.
     *
     * @param executionResult result related to contract execution procedure.
     * @return result hash.
     */
    private String calculateExecutionHash(ExecutionResult executionResult) {
        HashFunction function = Hashing.sha256();
        return function.hashString(executionResult.toString(), Charsets.UTF_8).toString();
    }

    /**
     * get input utxo
     *
     * @param input tx input
     * @return tx input ref output
     */
    public TransactionOutput getPreOutput(String preBlockHash, TransactionInput input) {
        String preOutKey = input.getPrevOut().getKey();
        if (org.apache.commons.lang3.StringUtils.isEmpty(preOutKey)) {
            LOGGER.info("preOutKey is empty,input={}", input.toJson());
            return null;
        }

        UTXO utxo;
        utxo = utxoServiceProxy.getUnionUTXO(preBlockHash, preOutKey);

        if (utxo == null) {
            LOGGER.info("UTXO is empty,input={},preOutKey={}", input.toJson(), preOutKey);
            return null;
        }
        TransactionOutput output = utxo.getOutput();
        return output;
    }

}


