package http.test;

import cn.primeledger.cas.global.blockchain.transaction.*;
import cn.primeledger.cas.global.crypto.ECKey;
import cn.primeledger.cas.global.utils.AmountUtils;
import com.google.common.collect.Lists;
import org.apache.commons.collections4.CollectionUtils;

import java.math.BigDecimal;
import java.util.List;

/**
 * @author Su Jiulong
 * @date 2018/3/9
 */
public class IssueTokenStakeTxCreaterHttp {

    //private key
    private static String priKey = "1954b19a2f78e1a1b5a42bdc042e66a671152cc7a3ccab40b1bca14685a6d962";
    //public key
    private static String pubKey = "03db15ebf22d4c997e189d684782739ca517b078d5890b2f87bd091a0641a9e1b3";
    private static String otherAddress = ECKey.pubKey2Base58Address("03bc7747eb46b1f3ae64e087c80e97a7137206ef8e0cf940ee47200a87d9ef1d2d");

    /**
     * @param utxos
     * @param otherAddress :My other address.
     * @param lockTime
     * @param version
     * @param amount       : The removal of the miners.
     * @param fee          : commission fees
     * @return
     */
    public static Transaction buildIssueTokenStakeMineTx(List<UTXO> utxos, String otherAddress, long lockTime, short version, BigDecimal amount, BigDecimal fee) {
        if (!ECKey.checkBase58Addr(otherAddress)) {
            throw new RuntimeException("Incorrect address format.");
        }
        if (!AmountUtils.check(false, fee)) {
            throw new RuntimeException("fee is null or more than less 0 or more than maximum");
        }
        if (!AmountUtils.check(false, amount)) {
            throw new RuntimeException("amount is null or more than less 0 or more than maximum");
        }
        if (lockTime < 0L) {
            throw new RuntimeException("The lockTime more than less 0");
        }
        if (version < 0) {
            throw new RuntimeException("The version more than less 0");
        }

        String address = ECKey.pubKey2Base58Address(pubKey);
        Transaction minerTx = new Transaction();
        minerTx.setVersion(version);
        minerTx.setLockTime(lockTime);
        //根据指定的地址获取对应的UTXOS
//        List<UTXO> utxos = utxoMap.values().stream().filter(utxo -> StringUtils.equals(address, utxo.getAddress())).collect(Collectors.toList());

        if (CollectionUtils.isEmpty(utxos)) {
            throw new RuntimeException("The incoming utxos is empty.");
        }
        //Transfer fee
        BigDecimal casFee = new BigDecimal("0");
        //Mining rights
        BigDecimal issueTokenFee = new BigDecimal("0");

        List<TransactionInput> inputs = Lists.newArrayList();
        TransactionInput feeInput = null;
        TransactionInput issueTokenInput = null;
        for (UTXO utxo : utxos) {
            if (null == utxo) {
                continue;
            }
            if (utxo.isCASCurrency()) {
                //Collect the UTXO of the CAS currency.
                BigDecimal tempFee = utxo.getOutput().getAmount();
                //Whether the total amount of CAS currency collected is greater than one.
                if (tempFee.compareTo(BigDecimal.ZERO) > 0) {
                    casFee = casFee.add(tempFee);
                    feeInput = StopOrRestartMineTxCreaterHttp.buildTransactionInput(utxo);
                    inputs.add(feeInput);
                }
            }
            if (utxo.isIssueTokenCurrency()) {
                //Collect the UTXO of the MINER currency.
                BigDecimal tempFee = utxo.getOutput().getAmount();
                //Whether the total amount of MINER currency collected is greater than one.
                if (tempFee.compareTo(BigDecimal.ZERO) > 0) {
                    issueTokenFee = issueTokenFee.add(tempFee);
                    issueTokenInput = StopOrRestartMineTxCreaterHttp.buildTransactionInput(utxo);
                    inputs.add(issueTokenInput);
                }
            }
        }
        if (CollectionUtils.isEmpty(inputs)) {
            throw new RuntimeException("There is no UTXO available.");
        }
        minerTx.setInputs(inputs);

        //output
        List outputList = Lists.newArrayList();
        //Transfer some of the ISSUE_TOKEN to another address.
        TransactionOutput minerOutput2OtherAddress = StopOrRestartMineTxCreaterHttp.generateTransactionOutput(otherAddress, SystemCurrencyEnum.ISSUE_TOKEN, amount);
        if (minerOutput2OtherAddress != null) {
            outputList.add(minerOutput2OtherAddress);
        }
        //MINER change
        TransactionOutput minerOutput2Address = StopOrRestartMineTxCreaterHttp.generateTransactionOutput(address, SystemCurrencyEnum.ISSUE_TOKEN, issueTokenFee.subtract(amount));
        if (minerOutput2Address != null) {
            outputList.add(minerOutput2Address);
        }

        //CAS change
        TransactionOutput casRest = StopOrRestartMineTxCreaterHttp.generateTransactionOutput(address, SystemCurrencyEnum.CAS, casFee.subtract(fee));
        if (null != casRest) {
            outputList.add(casRest);
        }

        if (CollectionUtils.isEmpty(outputList)) {
            return null;
        }
        minerTx.setOutputs(outputList);
        UpdateAddressTxCreaterHttp.signAndSetUnLockScript(inputs, minerTx, priKey, pubKey);
        return minerTx;
    }


    public static void main(String[] args) throws Exception {
        String ip = "localhost";
        int port = 8081;

        //Gets the corresponding UTXO according to the specified address.
        List<UTXO> list = TransactionCASTxCreaterHttp.getUTXOSByAddress(ECKey.pubKey2Base58Address(pubKey), ip, port);

        //build transaction
        Transaction issueTokenStakeMineTx = buildIssueTokenStakeMineTx(list, otherAddress,0,(short)0,BigDecimal.ONE,new BigDecimal("0.5"));

        System.out.println(list.stream().toString());

        //send transaction
        TransactionCASTxCreaterHttp.sendTx(issueTokenStakeMineTx, ip, port);

    }
}
