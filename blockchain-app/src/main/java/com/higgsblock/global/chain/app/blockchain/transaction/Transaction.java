package com.higgsblock.global.chain.app.blockchain.transaction;

import com.alibaba.fastjson.annotation.JSONType;
import com.google.common.base.Charsets;
import com.google.common.hash.HashFunction;
import com.google.common.hash.Hashing;
import com.higgsblock.global.chain.app.common.constants.MessageType;
import com.higgsblock.global.chain.app.common.message.Message;
import com.higgsblock.global.chain.app.contract.ContractExecutionResult;
import com.higgsblock.global.chain.app.contract.ContractParameters;
import com.higgsblock.global.chain.app.utils.AddrUtil;
import com.higgsblock.global.chain.app.utils.ISizeCounter;
import com.higgsblock.global.chain.app.utils.JsonSizeCounter;
import com.higgsblock.global.chain.common.entity.BaseSerializer;
import com.higgsblock.global.chain.common.utils.Money;
import com.higgsblock.global.chain.crypto.utils.CryptoUtils;
import com.higgsblock.global.chain.vm.fee.FeeUtil;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.util.Strings;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

/**
 * @author yuguojia
 * @date 2018/03/08
 **/
@Data
@Slf4j
@NoArgsConstructor
@Message(MessageType.TRANSACTION)
@JSONType(includes = {"version", "lockTime", "extra", "inputs", "outputs", "transactionTime", "gasPrice", "gasLimit",
        "contractParameters", "contractExecutionResult", "subTransaction"})
public class Transaction extends BaseSerializer {

    private static final int LIMITED_SIZE_UNIT = 1024 * 100;
    private static final int EXTRA_LIMITED_SIZE_UNIT = 1024 * 10;
    private static final int INIT_VERSION = 0;

    private int version;

    /**
     * the hash of this transaction
     */
    protected String hash;
    /**
     * lock after pointed block height of time
     */
    protected long lockTime;
    /**
     * extra info for this transaction
     */
    protected String extra;
    /**
     * the sources of current spending
     */
    private List<TransactionInput> inputs;
    /**
     * transfer to other coin
     */
    private List<TransactionOutput> outputs;

    /**
     * the timestamp of this transaction created
     */
    private long transactionTime = System.currentTimeMillis();

    /**
     * Gas price of a unit transaction creator is willing to pay.
     */
    private BigInteger gasPrice;
    /**
     * Maximum of gas amount for transaction being accepted.
     */
    private long gasLimit;

    /**
     * Parameters container for contract creation or contract call
     */
    private ContractParameters contractParameters;

    /**
     * Records status after contract being executed
     */
    private ContractExecutionResult contractExecutionResult;

    /**
     * invoked contract result transaction is true other false;
     */
    private boolean subTransaction;


    public ContractExecutionResult getContractExecutionResult() {
        if (contractExecutionResult == null) {
            contractExecutionResult = new ContractExecutionResult();
        }
        return contractExecutionResult;
    }

    public byte[] calculateContractAddress() {
        HashFunction function = Hashing.sha256();
        StringBuilder builder = new StringBuilder();
        builder.append(function.hashLong(transactionTime));
        builder.append(function.hashBytes(contractParameters.getBytecode()));
        builder.append(function.hashString(getInputsHash(), Charsets.UTF_8));

        return CryptoUtils.sha256hash160(function.hashString(builder, Charsets.UTF_8).asBytes());
    }

    public byte[] getContractAddress() {
        if (!isContractTrasaction()) {
            return null;
            //throw new IllegalArgumentException("There is no contract address because transaction does not contain a contract.");
        }

        return AddrUtil.toContractAddr(outputs.get(0).getLockScript().getAddress());

    }

    public boolean valid() {
        if (version < INIT_VERSION) {
            return false;
        }

        if (lockTime < 0) {
            return false;
        }

        if (gasLimit < 0) {
            return false;
        }

        if (contractParameters != null && !contractParameters.valid()) {
            return false;
        }

        if (CollectionUtils.isEmpty(outputs)) {
            return false;
        }

        if (CollectionUtils.isNotEmpty(inputs) && !subTransaction) {
            if (gasPrice == null || gasPrice.compareTo(BigInteger.valueOf(0L)) < 0) {
                return false;
            }
            for (TransactionInput input : inputs) {
                if (!input.valid()) {
                    return false;
                }
            }
        }

        for (TransactionOutput out : outputs) {
            if (!out.valid()) {
                return false;
            }
        }

        BigInteger sizeGas = FeeUtil.getSizeGas(getSize());
        if (sizeGas.compareTo(BigInteger.valueOf(gasLimit)) > 0) {
            return false;
        }

        return true;
    }

    public boolean validContractPart() {
        try {
            if (!isContractTrasaction()) {
                return true;
            }
        } catch (Exception e) {
            return false;
        }

        if (!contractParameters.valid()) {
            return false;
        }

        if (isContractCreation() && !Arrays.equals(getContractAddress(), calculateContractAddress())) {
            return false;
        }

        return true;
    }

    public String getHash() {
        if (StringUtils.isBlank(hash)) {
            HashFunction function = Hashing.sha256();
            StringBuilder builder = new StringBuilder();
            builder.append(function.hashInt(version));
            builder.append(function.hashLong(transactionTime));
            builder.append(function.hashLong(lockTime));
            builder.append(function.hashString(null == extra ? Strings.EMPTY : extra, Charsets.UTF_8));
            builder.append(function.hashString(getInputsHash(), Charsets.UTF_8));
            builder.append(function.hashString(getOutputsHash(), Charsets.UTF_8));
            builder.append(function.hashBytes(gasPrice != null ? gasPrice.toByteArray() : new byte[0]));
            builder.append(function.hashLong(gasLimit));
            builder.append(function.hashString(getContractParametersHash(), Charsets.UTF_8));
            hash = function.hashString(builder, Charsets.UTF_8).toString();
        }
        return hash;
    }

    private String getInputsHash() {
        HashFunction function = Hashing.sha256();
        if (CollectionUtils.isEmpty(inputs)) {
            return function.hashInt(0).toString();
        }

        StringBuilder builder = new StringBuilder();
        inputs.forEach(input -> builder
                .append(input.getHash())
        );
        return function.hashString(builder, Charsets.UTF_8).toString();
    }

    private String getOutputsHash() {
        HashFunction function = Hashing.sha256();
        if (CollectionUtils.isEmpty(outputs)) {
            return function.hashInt(0).toString();
        }

        StringBuilder builder = new StringBuilder();
        outputs.forEach(output -> builder
                .append(output.getHash())
        );
        return function.hashString(builder, Charsets.UTF_8).toString();
    }

    private String getContractParametersHash() {
        HashFunction function = Hashing.sha256();
        if (contractParameters == null) {
            return function.hashInt(0).toString();
        }

        StringBuilder builder = new StringBuilder();
        builder.append(function.hashInt(contractParameters.getVmVersion()));
        builder.append(function.hashBytes(contractParameters.getBytecode()));
        return function.hashString(builder, Charsets.UTF_8).toString();
    }

    public TransactionOutput getTransactionOutputByIndex(short index) {
        int size = outputs.size();
        if (size <= index + 1 || index < 0) {
            return null;
        }
        return outputs.get(index);
    }

    /**
     * Gets size of this transaction.
     *
     * @return size of this transaction.
     */
    public long getSize() {
        // extract state of transaction counted for size.
        Transaction origin = new Transaction();
        origin.version = this.version;
        origin.hash = this.hash;
        origin.lockTime = this.lockTime;
        origin.extra = this.extra;
        origin.inputs = this.inputs;
        origin.outputs = this.outputs;
        origin.transactionTime = this.transactionTime;
        origin.gasPrice = this.gasPrice;
        origin.gasLimit = this.gasLimit;
        origin.contractParameters = this.contractParameters;

        ISizeCounter sizeCounter = JsonSizeCounter.getJsonSizeCounter();
        return sizeCounter.calculateSize(origin);
    }

    public boolean sizeAllowed() {
        ISizeCounter sizeCounter = JsonSizeCounter.getJsonSizeCounter();
        if (sizeCounter.calculateSize(extra) > EXTRA_LIMITED_SIZE_UNIT) {
            return false;
        }
        if (sizeCounter.calculateSize(this) > LIMITED_SIZE_UNIT) {
            return false;
        }
        return true;
    }

    public boolean isEmptyInputs() {
        if (CollectionUtils.isEmpty(inputs)) {
            return true;
        }
        return false;
    }

    public List<String> getSpendUTXOKeys() {
        List result = new LinkedList();
        if (!isEmptyInputs()) {
            for (TransactionInput input : inputs) {
                result.add(input.getPreUTXOKey());
            }
        }

        return result;
    }

    public List<UTXO> getSpendUTXOs() {
        List<UTXO> result = new LinkedList();
        if (!isEmptyInputs()) {
            for (TransactionInput input : inputs) {
                TransactionOutPoint prevOutPoint = input.getPrevOut();
                TransactionOutput output = prevOutPoint.getOutput();
                UTXO spendUtxo = new UTXO(prevOutPoint.getTransactionHash(),
                        prevOutPoint.getIndex(), output);
                result.add(spendUtxo);
            }
        }

        return result;
    }

    public List<UTXO> getAddedUTXOs() {
        List result = new LinkedList();
        if (CollectionUtils.isNotEmpty(outputs)) {
            final int outputSize = outputs.size();
            for (int i = 0; i < outputSize; i++) {
                TransactionOutput output = outputs.get(i);
                UTXO utxo = new UTXO(this, (short) i, output);
                result.add(utxo);
            }
        }

        return result;
    }

    public boolean containsSpendUTXO(String utxoKey) {
        if (isEmptyInputs()) {
            return false;
        }
        for (TransactionInput input : inputs) {
            if (StringUtils.equals(input.getPreUTXOKey(), utxoKey)) {
                return true;
            }
        }
        return false;
    }

    public boolean isContractCreation() {
        return outputs.get(0).getLockScript().getType() == ScriptTypeEnum.CREATE.getType();
    }

    public boolean isContractCall() {
        return outputs.get(0).getLockScript().getType() == ScriptTypeEnum.CALL.getType();
    }

    public boolean isContractTrasaction() {
        return isContractCreation() || isContractCall();
    }

    public Money getFirstOutMoney() {
        return this.getOutputs().get(0).getMoney();
    }
}