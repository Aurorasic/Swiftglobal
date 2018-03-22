package http.test;

import cn.primeledger.cas.global.api.vo.ResponseData;
import cn.primeledger.cas.global.blockchain.transaction.*;
import cn.primeledger.cas.global.constants.RespCodeEnum;
import cn.primeledger.cas.global.crypto.ECKey;
import cn.primeledger.cas.global.network.http.client.HttpClient;
import cn.primeledger.cas.global.script.LockScript;
import cn.primeledger.cas.global.script.UnLockScript;
import cn.primeledger.cas.global.utils.AmountUtils;
import com.alibaba.fastjson.JSON;
import com.google.common.collect.Lists;
import http.api.ITransactionTestApi;
import http.api.IUTXOTestApi;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * @author kongyu
 * @date 2018/3/20
 */

@Slf4j
public class TransactionCASTxCreaterHttp {

    //转出私钥
    private static String priKey = "6f297284275fe7d774977dd79d20496b3b8fc0405f64f28033842da74403ecb5";

    //转入地址
    private static String outAddress = "1F7ygFQafFmpnaAWpRsh6RpAzQXWtCWXkE";

    //转出地址
    private static String inAddress = ECKey.fromPrivateKey(priKey).toBase58Address();

    private static short version = 1;
    private final static String IP = "127.0.0.1";
    private final static int PORT = 8084;

    public static void main(String[] args) throws Exception {
        //根据指定的地址获取对应的UTXOS
        List<UTXO> list = getUTXOSByAddress("1P453kHG2nb9P8LebFih1uXyLxU5D1GLwr", IP, PORT);

        System.out.println(list.stream().toString());

        //构造交易
        Transaction tx = buildTransaction(list, new BigDecimal(100), outAddress, 0, version, null);

        //发送交易
        sendTx(tx, IP, PORT);
    }

    /**
     * 转CAS币
     *
     * @param utxos
     * @param amount
     * @param address
     * @param lockTime
     * @param version
     * @param extra
     * @return
     */
    public static Transaction buildTransaction(List<UTXO> utxos, BigDecimal amount, String address, long lockTime, short version, String extra) {
        if (CollectionUtils.isEmpty(utxos)) {
            throw new RuntimeException("list is empty");
        }
        if (!AmountUtils.check(false, amount)) {
            throw new RuntimeException("the amount need more than zero");
        }
        ECKey casKey = ECKey.fromPrivateKey(priKey);
        Transaction transaction = new Transaction();
        transaction.setVersion(version);
        transaction.setExtra(extra);
        transaction.setLockTime(lockTime);

        //转账总金额
        BigDecimal amt = amount;

        List<TransactionOutput> newOutput = new ArrayList<>();
        List<TransactionInput> newInput = new ArrayList<>();
        transaction.setInputs(newInput);
        transaction.setOutputs(newOutput);

        TransactionOutput transactionOutput = new TransactionOutput();
        transactionOutput.setAmount(amount);
        LockScript lockScript = new LockScript();
        lockScript.setAddress(address);
        transactionOutput.setLockScript(lockScript);
        newOutput.add(transactionOutput);

        for (UTXO utxo : utxos) {
            if (!StringUtils.equals(utxo.getAddress(), casKey.toBase58Address())) {
                continue;
            }
            //只是发送cas币
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

            BigDecimal tempAmount = utxo.getOutput().getAmount();
            String currency = utxo.getOutput().getCurrency();
            transactionOutput.setCurrency(currency);

            if (tempAmount.compareTo(amt) == 0) {
                //don't have other output
                amt = amt.subtract(tempAmount);
                break;
            } else if (tempAmount.compareTo(amt) == -1) {
                amt = amt.subtract(tempAmount);
                continue;
            } else if (tempAmount.compareTo(amt) == 1) {//此处找零钱
                amt = amt.subtract(tempAmount);
                TransactionOutput temp = new TransactionOutput();
                temp.setAmount(amt.multiply(BigDecimal.valueOf(-1)));
                temp.setCurrency(currency);
                LockScript ls = new LockScript();
                ls.setAddress(casKey.toBase58Address());
                temp.setLockScript(ls);
                newOutput.add(temp);
                break;
            }
        }
        if (amt.compareTo(BigDecimal.ZERO) > 0) {
            return null;
//            throw new RuntimeException("can not find enough UTXO");
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

        IUTXOTestApi api = HttpClient.getApi(ip, port, IUTXOTestApi.class);
        ResponseData<List<UTXO>> responseData = api.query(inAddress).execute().body();
        System.out.println(JSON.toJSONString(responseData, true));
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
