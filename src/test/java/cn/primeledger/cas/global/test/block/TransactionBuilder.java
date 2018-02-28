package cn.primeledger.cas.global.test.block;

import cn.primeledger.cas.global.blockchain.transaction.*;
import cn.primeledger.cas.global.crypto.ECKey;
import cn.primeledger.cas.global.crypto.model.KeyPair;
import cn.primeledger.cas.global.script.LockScript;
import cn.primeledger.cas.global.script.UnLockScript;
import com.google.common.collect.Lists;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.ListUtils;
import org.mapdb.DB;
import org.mapdb.HTreeMap;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * @author yangyi
 * @deta 2018/2/23
 * @description
 */
@Getter
@Setter
@Slf4j
public class TransactionBuilder {

    private String outAddress = "1F6x4G2gDmMm9WrgJfWPNuW2Kw1veRdHzf";

    private String priKey = "623ee5c4c0eb7fcdafe6f86234eb2629d34b0647dd25902dc86936501dc5370f";

    private String pubKey = "03a98fe3180115b4cc0f7b4186de59c15c0528844f178ba526557146410492a3e6";

    private String inAddress = "1LYHUfw91EjUtqdnSTYjUh7qS46TNCUZZT";
    //51f7710e3a1622c7792ece8a0f4c3358423e2cd0e334151d13e5c8601c071b75
    //02c5851f8968fd69226025b1f3c844fcbc5b58cdbf8b85d3d53e75552b4f59381e

    private BigDecimal amount;

    private String extra;

    private long lockTime= 1000;

    private short type=0;

    private short version=1;

    private DB db;

    public Transaction buildCoinBase(){
        if(amount == null){
            throw new RuntimeException("please set the amount");
        }
        if(amount.compareTo(BigDecimal.ZERO) <= 0){
            throw new RuntimeException("the amount need more than zero");
        }
        Transaction transaction = new Transaction();
        transaction.setVersion(version);
        transaction.setType(TransactionTypeEnum.COINBASE_MINE.getType());
        transaction.setLockTime(lockTime);

        TransactionOutput transactionOutput = new TransactionOutput();
        transactionOutput.setAmount(this.amount);
        LockScript lockScript = new LockScript();
        lockScript.setAddress(inAddress);
        transactionOutput.setLockScript(lockScript);
        List emptyList = ListUtils.EMPTY_LIST;
        emptyList.add(transactionOutput);
        transaction.setOutputs(emptyList);
        return transaction;
    }

    public Transaction build() {
        if(amount == null){
            throw new RuntimeException("please set the amount");
        }
        if(amount.compareTo(BigDecimal.ZERO) <= 0){
            throw new RuntimeException("the amount need more than zero");
        }
        if(db == null){
            throw new RuntimeException("please set the db");
        }
        Transaction transaction = new Transaction();
        transaction.setVersion(version);
        transaction.setType(TransactionTypeEnum.COINBASE_MINE.getType());
        transaction.setLockTime(lockTime);
        HTreeMap<String, UTXO> treeMap = (HTreeMap<String, UTXO>) db.hashMap(outAddress).createOrOpen();
        Iterator<String> stringIterator = treeMap.keySet().iterator();

        BigDecimal amt = getAmount();
        List<String> spendUTXOKey = new ArrayList<>();
        List<TransactionOutput> newOutpt = new ArrayList<>();
        List<TransactionInput> newInput = new ArrayList<>();
        transaction.setInputs(newInput);
        transaction.setOutputs(newOutpt);

        TransactionOutput transactionOutput = new TransactionOutput();
        transactionOutput.setAmount(this.amount);
        LockScript lockScript = new LockScript();
        lockScript.setAddress(inAddress);
        transactionOutput.setLockScript(lockScript);
        newOutpt.add(transactionOutput);

        KeyPair key = new KeyPair();
        key.setAddress(outAddress);
        key.setPriKey(priKey);
        key.setPubKey(pubKey);
        ECKey casKey = ECKey.fromPrivateKey(key);

        BigDecimal fee = BigDecimal.ZERO;
        TransactionOutput feeOutput = new TransactionOutput();
        feeOutput.setAmount(fee);
        newOutpt.add(feeOutput);

        while (stringIterator.hasNext()) {
            String next = stringIterator.next();
            UTXO utxo = treeMap.get(next);
            spendUTXOKey.add(next);
            BigDecimal amount = utxo.getAmount();
            transactionOutput.setCurrency(utxo.getCurrency());

            TransactionInput transactionInput = new TransactionInput();
            TransactionOutPoint transactionOutPoint = new TransactionOutPoint();
            transactionOutPoint.setHash(utxo.getHash());
            transactionOutPoint.setIndex(utxo.getIndex());
            transactionInput.setPrevOut(transactionOutPoint);
            UnLockScript unLockScript = new UnLockScript();
            List<String> pubKeyList = Lists.newArrayList();
            List<String> sigList = Lists.newArrayList();
            unLockScript.setPkList(pubKeyList);
            unLockScript.setSigList(sigList);
            transactionInput.setUnLockScript(unLockScript);
            newInput.add(transactionInput);

            if (amount.compareTo(amt) == 0) {
                //don't have other output
                break;
            } else if (amount.compareTo(amt) == -1) {
                amt = amount.subtract(amt);
            } else if (amount.compareTo(amt) == 1) {
                amt = amount.subtract(amt);
                TransactionOutput temp = new TransactionOutput();
                temp.setAmount(amt);
                temp.setCurrency(utxo.getCurrency());
                LockScript ls = new LockScript();
                ls.setAddress(outAddress);
                temp.setLockScript(ls);
                newOutpt.add(temp);
                break;
            }
        }
        String hash = transaction.getHash();
        String signMessage = casKey.signMessage(hash);
        newInput.forEach(transactionInput -> {
            UnLockScript unLockScript = transactionInput.getUnLockScript();
            if(unLockScript != null){
                unLockScript.getPkList().add(casKey.getKeyPair().getPubKey());
                unLockScript.getSigList().add(signMessage);
            }
        });
        spendUTXOKey.forEach((used)->{
            treeMap.remove(used);
        });
        db.commit();
        return transaction;
    }

}
