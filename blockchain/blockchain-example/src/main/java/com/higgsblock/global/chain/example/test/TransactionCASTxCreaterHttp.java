package com.higgsblock.global.chain.example.test;

import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Lists;
import com.higgsblock.global.chain.app.api.vo.ResponseData;
import com.higgsblock.global.chain.app.blockchain.transaction.*;
import com.higgsblock.global.chain.app.constants.RespCodeEnum;
import com.higgsblock.global.chain.app.script.LockScript;
import com.higgsblock.global.chain.app.script.UnLockScript;
import com.higgsblock.global.chain.common.utils.Money;
import com.higgsblock.global.chain.crypto.ECKey;
import com.higgsblock.global.chain.example.api.ITransactionTestApi;
import com.higgsblock.global.chain.network.http.HttpClient;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * @author kongyu
 * @date 2018/3/20
 */

@Slf4j
public class TransactionCASTxCreaterHttp {

    private final static String ZERO = "0";

    /**
     * 转出私钥
     */
    private static String priKey = "a525251d56f20a921cda6cd9d71511d4a0edb02e3a0783445e3ab96e100a3daa";

    /**
     * 转入地址
     */
    private static String outAddress = "1HJQyN7q4qsXczkzwQkeon3S6YMixk1v82";

    /**
     * 转出地址
     */
    private static String inAddress = ECKey.fromPrivateKey(priKey).toBase58Address();

    private static short version = 1;
    private final static String IP = "192.168.10.71";
    private final static int PORT = 8081;

    public static void main(String[] args) throws Exception {
        while (true) {
            try {
                List<UTXO> list = getUTXOSByAddress("1PRCzE9s7ZWVN5vC44oNjPP9J4PNUooX4f", IP, PORT);
                Transaction tx = buildTransaction(list, new Money(1 + RandomStringUtils.randomNumeric(1)), outAddress, 0, version, "");

                if (null == tx) {
                    System.out.println("null == tx");
                    continue;
                }

                System.out.println("--------------------------------------------------");
                System.out.println(JSONObject.toJSONString(tx, true));

                sendTx(tx, IP, PORT);

                TimeUnit.SECONDS.sleep(3);
            } catch (Exception e) {
                System.out.println(e);
            }
        }
    }

    /**
     * 转CAS币
     *
     * @param utxos
     * @param money
     * @param address
     * @param lockTime
     * @param version
     * @param extra
     * @return
     */
    public static Transaction buildTransaction(List<UTXO> utxos, Money money, String address, long lockTime, short version, String extra) {
        if (CollectionUtils.isEmpty(utxos)) {
            throw new RuntimeException("list is empty");
        }
        if (!money.checkRange()) {
            throw new RuntimeException("Please check currency and amount of money," +
                    "currency must is not empty, amount must more than less 0");
        }
        ECKey casKey = ECKey.fromPrivateKey(priKey);
        Transaction transaction = new Transaction();
        transaction.setVersion(version);
        transaction.setCreatorPubKey(casKey.getKeyPair().getPubKey());
        transaction.setExtra(extra);
        transaction.setLockTime(lockTime);

        //转账总金额
        Money amt = new Money("0.01").add(money);

        List<TransactionOutput> newOutput = new ArrayList<>();
        List<TransactionInput> newInput = new ArrayList<>();
        transaction.setInputs(newInput);
        transaction.setOutputs(newOutput);

        TransactionOutput transactionOutput = new TransactionOutput();
        transactionOutput.setMoney(money);
        LockScript lockScript = new LockScript();
        lockScript.setAddress(address);
        transactionOutput.setLockScript(lockScript);
        newOutput.add(transactionOutput);

        for (UTXO utxo : utxos) {
            if (!StringUtils.equals(utxo.getAddress(), casKey.toBase58Address())) {
                continue;
            }

            if (!utxo.isCASCurrency()) {
                continue;
            }

            TransactionInput transactioninput = new TransactionInput();
            TransactionOutPoint txOutPoint = new TransactionOutPoint();
            txOutPoint.setHash(utxo.getHash());
            txOutPoint.setIndex(utxo.getIndex());
            transactioninput.setPrevOut(txOutPoint);
            newInput.add(transactioninput);

            UnLockScript unLockScript = new UnLockScript();
            unLockScript.setPkList(Lists.newArrayList());
            unLockScript.setSigList(Lists.newArrayList());
            transactioninput.setUnLockScript(unLockScript);

            Money tempAmount = utxo.getOutput().getMoney();

            if (tempAmount.compareTo(amt) == 0) {
                //don't have other output
                amt = amt.subtract(tempAmount);
                break;
            } else if (tempAmount.compareTo(amt) == -1) {
                amt = amt.subtract(tempAmount);
                continue;
            } else if (tempAmount.compareTo(amt) == 1) {
                tempAmount = tempAmount.subtract(amt);
                amt = new Money("0");
                TransactionOutput temp = new TransactionOutput();
                temp.setMoney(tempAmount);
                LockScript ls = new LockScript();
                ls.setAddress(casKey.toBase58Address());
                temp.setLockScript(ls);
                newOutput.add(temp);
                break;
            }
        }
        if (amt.compareTo(new Money(ZERO)) > 0) {
            return null;
        }
        String hash = transaction.getHash();
        String signMessage = casKey.signMessage(hash);

        transaction.getInputs().forEach(input -> {
            input.getUnLockScript().getPkList().add(casKey.getKeyPair().getPubKey());
            input.getUnLockScript().getSigList().add(signMessage);
        });
        return transaction;
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
}
