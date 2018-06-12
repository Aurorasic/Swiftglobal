package com.higgsblock.global.chain.example.test;

import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Lists;
import com.higgsblock.global.chain.app.api.vo.ResponseData;
import com.higgsblock.global.chain.app.blockchain.transaction.*;
import com.higgsblock.global.chain.app.constants.RespCodeEnum;
import com.higgsblock.global.chain.app.script.LockScript;
import com.higgsblock.global.chain.app.script.UnLockScript;
import com.higgsblock.global.chain.common.enums.SystemCurrencyEnum;
import com.higgsblock.global.chain.common.utils.Money;
import com.higgsblock.global.chain.crypto.ECKey;
import com.higgsblock.global.chain.example.api.ITransactionTestApi;
import com.higgsblock.global.chain.network.http.HttpClient;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * @author Su Jiulong
 * @date 2018-3-28
 */
@Slf4j
public class TransactionCreaterHttp {

    /**private key*/
    private static String priKey = "b5b146e0e3fe97186ee6f31ac2ca609d9ccd0eb50672cfb7c37803626ebac73c";
    /**public key*/
    private static String pubKey = "038fe189372eea68870f13e5b766b66af3770a98d43e83aa48c41a270ac7ca500d";
    /**new address*/
    private static String otherAddress = ECKey.pubKey2Base58Address("03e2576529b8e999a551e9ba46ad391b35200b3e4a7485fdc1e5322d9167bf7b48");

    public static void main(String[] args) throws Exception {
        String ip = "192.168.11.153";
        int port = 7081;

        List<String> otherAddresses = Lists.newArrayList();
        otherAddresses.add(otherAddress);
        while (true) {
            try {
                List<UTXO> list = getUTXOSByAddress("18h8qEyUSkBcvWGefSpXB9t6VX9gxaWfcA", ip, port);
                Money money = new Money();
                list.forEach(utxo -> {
                    if (utxo.getOutput().getMoney().getCurrency().equals(SystemCurrencyEnum.CAS.getCurrency())) {
                        money.add(utxo.getOutput().getMoney());
                    }
                });
                System.out.println("money:{}" + money.getValue());
                //build transaction
                Money casMoney = new Money("0.75647839");
                Transaction transactionCAS = buildCASTransaction(list, otherAddresses, 0L,
                        (short) 0, "transfer cas", casMoney, new Money("0.33757177"));

                System.out.println("--------------------------------------------------");
                System.out.println(JSONObject.toJSONString(transactionCAS, true));

                //发送交易
                sendTx(transactionCAS, ip, port);
                // break;
                TimeUnit.SECONDS.sleep(1);
            } catch (Exception e) {
                LOGGER.error("ex:{}", e);
            }
        }
    }

    public static Transaction buildTransaction(List<UTXO> utxos, List<String> otherAddresses, long lockTime,
                                               short version, String extra, Money money, Money fee) {
        if (CollectionUtils.isEmpty(utxos)) {
            throw new RuntimeException("The incoming utxos is empty.");
        }
        if (CollectionUtils.isEmpty(otherAddresses)) {
            throw new RuntimeException("The otherAddresses is empty");
        }
        if (lockTime < 0L) {
            throw new RuntimeException("The lockTime more than less 0");
        }
        if (version < 0) {
            throw new RuntimeException("The version more than less 0");
        }
        if (!money.checkRange()) {
            throw new RuntimeException("Please check currency and amount of money," +
                    "currency must is not empty, amount must more than less 0");
        }
        if (!fee.checkRange()) {
            throw new RuntimeException("fee is null or more than less 0 or more than maximum");
        }

        if (money.getCurrency().equals(SystemCurrencyEnum.CAS.getCurrency())) {
            return buildCASTransaction(utxos, otherAddresses, lockTime, version, extra, money, fee);
        } else {
            return buildOtherTransaction(utxos, otherAddresses, lockTime, version, extra, money, fee);
        }
    }

    /**
     * building transaction input
     *
     * @param utxo
     * @return
     */
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

    /**
     * build single transaction output
     *
     * @param address
     * @param money
     * @return
     */
    public static TransactionOutput generateTransactionOutput(String address, Money money) {
        if (!ECKey.checkBase58Addr(address)) {
            return null;
        }
        if (!money.checkRange()) {
            return null;
        }
        TransactionOutput output = new TransactionOutput();
        output.setMoney(money);
        LockScript lockScript = new LockScript();
        lockScript.setAddress(address);
        output.setLockScript(lockScript);
        return output;
    }

    /**
     * build multiple transaction output
     *
     * @param addresses
     * @param outputs
     * @param money
     * @return
     */
    private static List<TransactionOutput> generateTransactionOutputs(List<String> addresses, List<TransactionOutput> outputs, Money money) {
        if (CollectionUtils.isNotEmpty(addresses)) {
            for (String address : addresses) {
                TransactionOutput output = generateTransactionOutput(address, money);
                if (output != null) {
                    outputs.add(output);
                }
            }
        }
        return outputs;
    }

    private static List<TransactionOutput> balance(Money totalAmount, Money money, String address, List<TransactionOutput> outputs) {
        Money zeroMoney = new Money("0", money.getCurrency());
        if (!totalAmount.greaterThan(zeroMoney)) {
            return outputs;
        }
        if (!money.greaterThan(zeroMoney)) {
            return outputs;
        }
        if (!ECKey.checkBase58Addr(address)) {
            return outputs;
        }
        Money balance = new Money(totalAmount.getValue()).subtract(money);
        if (balance.greaterThan(zeroMoney)) {
            TransactionOutput casRest = generateTransactionOutput(address, balance);
            if (null != casRest) {
                outputs.add(casRest);
            }
        }
        return outputs;
    }

    /**
     * Set the signature and public key to the unlock script.
     *
     * @param inputs
     * @param transaction
     * @param priKey
     * @param pubKey
     */
    private static void signAndSetUnLockScript(List<TransactionInput> inputs, Transaction transaction, String priKey, String pubKey) {
        String signMessage = ECKey.signMessage(transaction.getHash(), priKey);
        inputs.forEach(transactionInput -> {
            UnLockScript unLockScript = transactionInput.getUnLockScript();
            if (unLockScript != null) {
                unLockScript.getPkList().add(pubKey);
                unLockScript.getSigList().add(signMessage);
            }
        });
    }

    public static List<UTXO> getUTXOSByAddress(String addr, String ip, int port) throws Exception {
        if (null == ip) {
            return null;
        }
        if (port <= 0) {
            return null;
        }

        ITransactionTestApi api = HttpClient.getApi(ip, port, ITransactionTestApi.class);
        ResponseData<List<UTXO>> responseData = api.queryUTXO(addr).execute().body();
        return responseData.getData();
    }

    /**
     * send transaction
     *
     * @param tx
     * @param ip
     * @param port
     * @throws Exception
     */
    public static void sendTx(Transaction tx, String ip, int port) throws Exception {
        if (null == ip) {
            throw new RuntimeException("ip is null");
        }
        if (port <= 0) {
            throw new RuntimeException("port is <= 0");
        }

        ITransactionTestApi api = HttpClient.getApi(ip, port, ITransactionTestApi.class);
        ResponseData<Boolean> responseData = api.sendTx(tx).execute().body();
        if (StringUtils.equals(responseData.getRespCode(), RespCodeEnum.SUCCESS.getCode())) {
            System.out.println("send tx success");
        } else {
            System.out.println("send tx failed");
        }
    }

    public static Transaction buildCASTransaction(List<UTXO> utxos, List<String> otherAddresses, long lockTime,
                                                  short version, String extra, Money money, Money fee) {

        Transaction transaction = new Transaction();
        transaction.setVersion(version);
        transaction.setCreatorPubKey(pubKey);
        transaction.setLockTime(lockTime);
        transaction.setExtra(extra);

        String address = ECKey.pubKey2Base58Address(pubKey);

        Money utxoTotal = new Money("0");
        Money usedTotal = new Money("0");
        usedTotal.add(money);
        usedTotal.add(fee);
        List<TransactionInput> inputs = Lists.newArrayList();
        for (UTXO utxo : utxos) {
            if (!StringUtils.equals(utxo.getAddress(), address)) {
                continue;
            }
            if (!utxo.getOutput().getMoney().getCurrency().equals(SystemCurrencyEnum.CAS.getCurrency())) {
                continue;
            }
            utxoTotal.add(utxo.getOutput().getMoney());
            inputs.add(buildTransactionInput(utxo));
            if (utxoTotal.compareTo(usedTotal) >= 0) {
                break;
            }
        }
        if (utxoTotal.compareTo(usedTotal) < 0) {
            throw new RuntimeException("CAS coin not enough");
        }
        transaction.setInputs(inputs);
        //output
        List<TransactionOutput> outputList = Lists.newArrayList();
        {
            //Transfer some of the CAS to another address.
            outputList = generateTransactionOutputs(otherAddresses, outputList, money);
            //CAS balance
            outputList = balance(utxoTotal, usedTotal, address, outputList);
        }

        if (CollectionUtils.isEmpty(outputList)) {
            return null;
        }

        transaction.setOutputs(outputList);

        signAndSetUnLockScript(inputs, transaction, priKey, pubKey);

        return transaction;
    }

    public static Transaction buildOtherTransaction(List<UTXO> utxos, List<String> otherAddresses, long lockTime,
                                                    short version, String extra, Money money, Money fee) {

        Transaction transaction = new Transaction();
        transaction.setVersion(version);
        transaction.setCreatorPubKey(pubKey);
        transaction.setLockTime(lockTime);
        transaction.setExtra(extra);

        String address = ECKey.pubKey2Base58Address(pubKey);

        Money utxoCasTotal = new Money("0");
        Money utxoTotal = new Money("0");


        List<TransactionInput> inputs = Lists.newArrayList();
        for (UTXO utxo : utxos) {
            if (!StringUtils.equals(utxo.getAddress(), address)) {
                continue;
            }

            if (utxo.getOutput().getMoney().getCurrency().equals(SystemCurrencyEnum.CAS.getCurrency())) {
                if (utxoCasTotal.compareTo(fee) < 0) {
                    utxoCasTotal.add(utxo.getOutput().getMoney());
                    inputs.add(buildTransactionInput(utxo));
                }
            } else {
                if (utxoTotal.compareTo(money) < 0) {
                    utxoTotal.add(utxo.getOutput().getMoney());
                    inputs.add(buildTransactionInput(utxo));
                }
            }

            if (utxoCasTotal.compareTo(fee) >= 0 && utxoTotal.compareTo(money) >= 0) {
                break;
            }

        }
        if (utxoCasTotal.compareTo(fee) < 0) {
            throw new RuntimeException("CAS fee coin not enough");
        }
        if (utxoTotal.compareTo(money) < 0) {
            throw new RuntimeException("CAS  not enough");
        }

        transaction.setInputs(inputs);
        //output
        List<TransactionOutput> outputList = Lists.newArrayList();
        {
            //Transfer some of the CAS to another address.
            generateTransactionOutputs(otherAddresses, outputList, money);
            //CAS balance
            balance(utxoCasTotal, fee, address, outputList);
            //tokenType balance
            balance(utxoTotal, money, address, outputList);
        }

        if (CollectionUtils.isEmpty(outputList)) {
            return null;
        }

        transaction.setOutputs(outputList);

        signAndSetUnLockScript(inputs, transaction, priKey, pubKey);

        return transaction;
    }
}
