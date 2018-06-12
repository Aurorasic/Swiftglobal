package com.higgsblock.global.browser.example.test;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.higgsblock.global.browser.example.utils.UrlUtils;
import com.higgsblock.global.browser.utils.HttpClient;
import com.higgsblock.global.browser.vo.UTXOVO;
import com.higgsblock.global.chain.app.blockchain.transaction.Transaction;
import com.higgsblock.global.chain.app.blockchain.transaction.TransactionInput;
import com.higgsblock.global.chain.app.blockchain.transaction.TransactionOutPoint;
import com.higgsblock.global.chain.app.blockchain.transaction.TransactionOutput;
import com.higgsblock.global.chain.app.script.LockScript;
import com.higgsblock.global.chain.app.script.UnLockScript;
import com.higgsblock.global.chain.common.enums.SystemCurrencyEnum;
import com.higgsblock.global.chain.common.utils.Money;
import com.higgsblock.global.chain.crypto.ECKey;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * @author Su Jiulong
 * @date 2018-3-28
 */
public class TransactionCreaterHttp {

    /**
     * private key
     */
    private static String priKey = "73ab56e6f83276c14d259b83dd83e455d6beb4bb617608d9f315b92111ee0a9d";
    /**
     * public key
     */
    private static String pubKey = "03d25c3b5fc0f48f300879d36597104860b0bc2c66bdc5e8bafb0426fd1cf9f396";
    /**
     * new address
     */
    private static String otherAddress = ECKey.pubKey2Base58Address("03e2576529b8e999a551e9ba46ad391b35200b3e4a7485fdc1e5322d9167bf7b48");
    //    private static String otherAddress1 = ECKey.PUBKEY2BASE58ADDRESS("0367a2279fc0910c3feca555461ddda7f9173f74da99e454fcc2f36d0bb4feff6a");

    public static void main(String[] args) throws Exception {
        String ip = "192.168.10.224";
        int port = 8081;

        List<String> otherAddresses = Lists.newArrayList();
        otherAddresses.add(otherAddress);

        try {
            List<UTXOVO> list = getUTXOSByAddress(ip, port, "1Lz9P1T8aVc1Eo6EoGKRrFAthvniotHYPh");
            Money money = new Money();
            list.forEach(utxo -> {
                if (utxo.getCurrency().equals(SystemCurrencyEnum.CAS.getCurrency())) {
                    money.add(utxo.getAmount());
                }
            });
            System.out.println("money:{}" + money.getValue());
            //build transaction
            Money casMoney = new Money("0.3");

            Transaction transactionCAS = buildTransaction(list, otherAddresses, 0L,
                    (short) 0, "transfer cas", casMoney, new Money("0.01"));


            System.out.println("--------------------------------------------------");
            System.out.println(JSONObject.toJSONString(transactionCAS, true));

            //发送交易
            sendTx(transactionCAS, "192.168.10.71", 8080);

            TimeUnit.SECONDS.sleep(5);
        } catch (Exception e) {
            System.out.println(e);
        }
    }

    public static Transaction buildTransaction(List<UTXOVO> utxos, List<String> otherAddresses, long lockTime,
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
        Transaction transaction = new Transaction();
        transaction.setVersion(version);
        transaction.setCreatorPubKey(pubKey);
        transaction.setLockTime(lockTime);
        transaction.setExtra(extra);

        String address = ECKey.pubKey2Base58Address(pubKey);

        //moneyCurrency
        String moneyCurrency = money.getCurrency();
        //Transfer fee
        Money casFee = new Money("0");
        //Mining fee
        Money tokenFee = new Money("0", moneyCurrency);

        List<TransactionInput> inputs = Lists.newArrayList();
        TransactionInput feeInput = null;
        TransactionInput tokenInput = null;
        //temp tokenMoney
        Money temptokenMoney = new Money(money.getValue(), moneyCurrency);
        //Sum of transfer tokenAmount and transfer fee.
        Money allTokenMoney = new Money(temptokenMoney.multiply(otherAddresses.size()).getValue(), moneyCurrency);
        //temp allTokenMoney
        Money tempAllTokenMoney = new Money(allTokenMoney.getValue(), moneyCurrency);
        //Sum of transfer casAmount and transfer fee.
        Money totalMoney = new Money(tempAllTokenMoney.add(fee.getValue()).getValue(), moneyCurrency);

        //Just to judge
        Money casZeroMoney = new Money("0");
        Money tokenZeroMoney = new Money("0", moneyCurrency);
        if (SystemCurrencyEnum.CAS.getCurrency().equals(moneyCurrency)) {
            for (UTXOVO utxo : utxos) {
                if (null == utxo) {
                    continue;
                }
                if (!StringUtils.equals(utxo.getAddress(), address)) {
                    continue;
                }


                if (isCASCurrency(utxo) && new Money(utxo.getAmount(), SystemCurrencyEnum.CAS.getCurrency()).greaterThan(casZeroMoney)) {
                    //Collect the UTXO of the CAS moneyCurrency.
                    casFee = casFee.add(utxo.getAmount());
                    feeInput = buildTransactionInput(utxo);
                    inputs.add(feeInput);
                    if (casFee.compareTo(totalMoney) >= 0) {
                        break;
                    }
                }
            }
            if (casFee.compareTo(totalMoney) < 0) {
                throw new RuntimeException("CAS coin not enough");
            }
        } else {
            //Whether the casFee is sufficient
            boolean casFlag = false;
            boolean tokenFlag = false;
            for (UTXOVO utxo : utxos) {
                if (null == utxo) {
                    continue;
                }
                if (!StringUtils.equals(utxo.getAddress(), address)) {
                    continue;
                }
                if (isCASCurrency(utxo)) {
                    if (!casFlag) {
                        if (new Money(utxo.getAmount(), SystemCurrencyEnum.CAS.getCurrency()).greaterThan(casZeroMoney)) {
                            //Collect the UTXO of the CAS moneyCurrency.
                            casFee = casFee.add(utxo.getAmount());
                            feeInput = buildTransactionInput(utxo);
                            inputs.add(feeInput);
                            if (casFee.compareTo(fee) >= 0) {
                                casFlag = true;
                            }
                        }
                    }
                } else if (!tokenFlag) {
                    if (utxo.getCurrency().equals(moneyCurrency) &&
                            new Money(utxo.getAmount(), SystemCurrencyEnum.CAS.getCurrency()).greaterThan(tokenZeroMoney)) {
                        //Collect the UTXO of the moneyCurrency.
                        tokenFee = tokenFee.add(new Money(utxo.getAmount(), SystemCurrencyEnum.CAS.getCurrency()));
                        tokenInput = buildTransactionInput(utxo);
                        inputs.add(tokenInput);
                        if (tokenFee.compareTo(allTokenMoney) >= 0) {
                            tokenFlag = true;
                        }
                    }
                }
                if (casFlag && tokenFlag) {
                    break;
                }
            }
            if (casFee.compareTo(fee) < 0) {
                throw new RuntimeException("CAS transfer fee not enough");
            }
            if (tokenFee.compareTo(allTokenMoney) < 0) {
                throw new RuntimeException("Token not enough");
            }
        }

        if (CollectionUtils.isEmpty(inputs)) {
            throw new RuntimeException("There is no UTXO available.");
        }
        transaction.setInputs(inputs);

        //output
        List<TransactionOutput> outputList = Lists.newArrayList();
        if (SystemCurrencyEnum.CAS.getCurrency().equals(moneyCurrency)) {
            //Transfer some of the CAS to another address.
            outputList = generateTransactionOutputs(otherAddresses, outputList, money);
            //CAS balance
            //outputList = balance(casFee, totalMoney, address, outputList);
        } else {
            //Transfer some of the tokenType to another address.
            outputList = generateTransactionOutputs(otherAddresses, outputList, money);
            //tokenType balance
            outputList = balance(tokenFee, allTokenMoney, address, outputList);
            //CAS balance
            outputList = balance(casFee, fee, address, outputList);
        }
        if (CollectionUtils.isEmpty(outputList)) {
            return null;
        }
        transaction.setOutputs(outputList);
        signAndSetUnLockScript(inputs, transaction, priKey, pubKey);
        return transaction;
    }

    private static boolean isCASCurrency(UTXOVO utxoVo) {
        if (null == utxoVo) {
            return false;
        }
        if (SystemCurrencyEnum.CAS.getCurrency().equals(utxoVo.getCurrency())) {
            return true;
        }
        return false;
    }

    /**
     * building transaction input
     *
     * @param utxo
     * @return
     */
    public static TransactionInput buildTransactionInput(UTXOVO utxo) {
        if (null == utxo) {
            return null;
        }
        TransactionInput intput = new TransactionInput();
        TransactionOutPoint feeOutPoint = new TransactionOutPoint();
        UnLockScript feeUnLockScript = new UnLockScript();
        feeUnLockScript.setPkList(Lists.newArrayList());
        feeUnLockScript.setSigList(Lists.newArrayList());
        feeOutPoint.setHash(utxo.getTransactionHash());
        feeOutPoint.setIndex(utxo.getOutIndex());
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
        Money balance = new Money(totalAmount.subtract(money).getValue(), totalAmount.getCurrency());
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

    public static List<UTXOVO> getUTXOSByAddress(String ip, int port, String address) throws Exception {
        if (null == ip) {
            return null;
        }
        if (port <= 0) {
            return null;
        }
        Map<String, Object> addressMap = Maps.newHashMap();
        addressMap.put("address", address);

        try {
            if (UrlUtils.ipPortCheckout(ip, port)) {
                String url = UrlUtils.builderUrl(ip, port, UrlUtils.GET_UTXOS);
                String result = HttpClient.getSync(url, addressMap);
                List<UTXOVO> utxoVos = (List<UTXOVO>) JSON.parseObject(result, UTXOVO.class);
                return utxoVos;
            }
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
        return null;
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

        Map<String, Object> txMap = Maps.newHashMap();
        txMap.put("transaction", tx);

        try {
            if (UrlUtils.ipPortCheckout(ip, port)) {
                String url = UrlUtils.builderUrl(ip, port, UrlUtils.SEND_TRANSACTION);
                String result = HttpClient.getSync(url, txMap);
                System.out.println(result);
            }
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
    }
}
