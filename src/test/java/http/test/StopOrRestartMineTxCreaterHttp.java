package http.test;

import cn.primeledger.cas.global.blockchain.transaction.*;
import cn.primeledger.cas.global.crypto.ECKey;
import cn.primeledger.cas.global.script.LockScript;
import cn.primeledger.cas.global.script.UnLockScript;
import cn.primeledger.cas.global.utils.AmountUtils;
import com.google.common.collect.Lists;
import org.apache.commons.collections4.CollectionUtils;

import java.math.BigDecimal;
import java.util.List;

/**
 * @author Su Jiulong
 * @date 2018/3/9
 */
public class StopOrRestartMineTxCreaterHttp {

    //private key
    private static String priKey = "a525251d56f20a921cda6cd9d71511d4a0edb02e3a0783445e3ab96e100a3daa";
    //public key
    private static String pubKey = "0377b85fbc137825bac7d933faf7b9807579c62afaf2cd462cc471a1ea2b14ed90";
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
    public static Transaction buildStopOrRestartMineTx(List<UTXO> utxos, String otherAddress, long lockTime, short version, BigDecimal amount, BigDecimal fee) {
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
        BigDecimal casFee = BigDecimal.ZERO;
        //Mining rights
        BigDecimal minerFee = BigDecimal.ZERO;

        List<TransactionInput> inputs = Lists.newArrayList();
        TransactionInput feeInput = null;
        TransactionInput minerInput = null;
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
                    feeInput = buildTransactionInput(utxo);
                    inputs.add(feeInput);
                }
            }
            if (utxo.isMinerCurrency()) {
                //Collect the UTXO of the MINER currency.
                BigDecimal tempFee = utxo.getOutput().getAmount();
                //Whether the total amount of MINER currency collected is greater than one.
                if (tempFee.compareTo(BigDecimal.ZERO) > 0) {
                    minerFee = minerFee.add(tempFee);
                    minerInput = buildTransactionInput(utxo);
                    inputs.add(minerInput);
                }
            }
        }
        if (CollectionUtils.isEmpty(inputs)) {
            throw new RuntimeException("There is no UTXO available.");
        }
        minerTx.setInputs(inputs);

        //output
        List outputList = Lists.newArrayList();
        //Transfer some of the MINER to another address.
        TransactionOutput minerOutput2OtherAddress = generateTransactionOutput(otherAddress, SystemCurrencyEnum.MINER, amount);
        if (minerOutput2OtherAddress != null) {
            outputList.add(minerOutput2OtherAddress);
        }
        //MINER change
        TransactionOutput minerOutput2Address = generateTransactionOutput(address, SystemCurrencyEnum.MINER, minerFee.subtract(amount));
        if (minerOutput2Address != null) {
            outputList.add(minerOutput2Address);
        }

        //CAS change
        TransactionOutput casRest = generateTransactionOutput(address, SystemCurrencyEnum.CAS, casFee.subtract(fee));
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

    public static TransactionInput buildTransactionInput(UTXO utxo) {
        if (null == utxo) {
            return null;
        }
        TransactionInput intput = new TransactionInput();
        TransactionOutPoint feeOutPoint = new TransactionOutPoint();
        UnLockScript feeUnLockScript = new UnLockScript();
        feeUnLockScript.setPkList(Lists.newArrayList());
        feeUnLockScript.setSigList(Lists.newArrayList());
        feeOutPoint.setHash(utxo.getHash());
        feeOutPoint.setIndex(utxo.getIndex());
        intput.setPrevOut(feeOutPoint);
        intput.setUnLockScript(feeUnLockScript);
        return intput;
    }

    public static TransactionOutput generateTransactionOutput(String address, SystemCurrencyEnum currencyEnum, BigDecimal amount) {
        if (!ECKey.checkBase58Addr(address)) {
            return null;
        }
        if (null == currencyEnum) {
            return null;
        }
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            return null;
        }
        TransactionOutput output = new TransactionOutput();
        output.setAmount(amount);
        output.setCurrency(currencyEnum.getCurrency());
        LockScript lockScript = new LockScript();
        lockScript.setAddress(address);
        output.setLockScript(lockScript);
        return output;
    }

    public static void main(String[] args) throws Exception {
        String ip = "localhost";
        int port = 8081;

        //Gets the corresponding UTXO according to the specified address.
        List<UTXO> list = TransactionCASTxCreaterHttp.getUTXOSByAddress(ECKey.pubKey2Base58Address(pubKey), ip, port);

        //build transaction
        Transaction stopOrRestartMineTransaction = buildStopOrRestartMineTx(list, otherAddress, 0L, (short) 0, new BigDecimal(0.5), BigDecimal.ONE);

        System.out.println(list.stream().toString());

        //发送交易
        TransactionCASTxCreaterHttp.sendTx(stopOrRestartMineTransaction, ip, port);

    }
}
