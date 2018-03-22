package http.test;

import cn.primeledger.cas.global.blockchain.transaction.*;
import cn.primeledger.cas.global.crypto.ECKey;
import cn.primeledger.cas.global.script.LockScript;
import cn.primeledger.cas.global.script.UnLockScript;
import cn.primeledger.cas.global.utils.AmountUtils;
import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * @author baizhengwen
 * @date 2018/3/5
 */

@Slf4j
public class TransactionMinerTxCreaterHttp {

    //private key
    private static String priKey = "a525251d56f20a921cda6cd9d71511d4a0edb02e3a0783445e3ab96e100a3daa";
    //public key
    private static String pubKey = "0377b85fbc137825bac7d933faf7b9807579c62afaf2cd462cc471a1ea2b14ed90";
    //new address
    private static String otherAddress = ECKey.pubKey2Base58Address("03bc7747eb46b1f3ae64e087c80e97a7137206ef8e0cf940ee47200a87d9ef1d2d");

    public static void main(String[] args) throws Exception {
        String ip = "localhost";
        int port = 8081;

        //Gets the corresponding UTXO according to the specified address.
        List<UTXO> list = TransactionCASTxCreaterHttp.getUTXOSByAddress(ECKey.pubKey2Base58Address(pubKey), ip, port);

        //build transaction
        Transaction transaction = buildTransaction(list, BigDecimal.ONE, otherAddress, 0, (short) 1, "extra");

        System.out.println(list.stream().toString());

        //send transaction
        TransactionCASTxCreaterHttp.sendTx(transaction, ip, port);
    }

    /**
     * 转MIENR币
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

        TransactionOutput TransactionOutput = new TransactionOutput();
        TransactionOutput.setAmount(amount);
        LockScript lockScript = new LockScript();
        lockScript.setAddress(address);
        TransactionOutput.setLockScript(lockScript);
        newOutput.add(TransactionOutput);

        for (UTXO utxo : utxos) {
            if (!StringUtils.equals(utxo.getAddress(), casKey.toBase58Address())) {
                continue;
            }
            //只是发送miner币
            if (!utxo.isMinerCurrency()) {
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
            TransactionOutput.setCurrency(currency);

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

}
