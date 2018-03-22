package http.test;

import cn.primeledger.cas.global.blockchain.transaction.*;
import cn.primeledger.cas.global.crypto.ECKey;
import cn.primeledger.cas.global.utils.AmountUtils;
import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;

import java.math.BigDecimal;
import java.util.List;

/**
 * @author baizhengwen
 * @date 2018/3/5
 */

@Slf4j
public class TransactionJoinMinerTxCreaterHttp {

    //private key
    private static String priKey = "a525251d56f20a921cda6cd9d71511d4a0edb02e3a0783445e3ab96e100a3daa";
    //public key
    private static String pubKey = "0377b85fbc137825bac7d933faf7b9807579c62afaf2cd462cc471a1ea2b14ed90";

    private final static BigDecimal MINER_AMOUNT = new BigDecimal("1.0");

    public static void main(String[] args) throws Exception {
        String ip = "localhost";
        int port = 8081;

        //Gets the corresponding UTXO according to the specified address.
        List<UTXO> list = TransactionCASTxCreaterHttp.getUTXOSByAddress(ECKey.pubKey2Base58Address(pubKey), ip, port);

        System.out.println(list.stream().toString());

        List<String> addrs = Lists.newArrayList();
        addrs.add(ECKey.pubKey2Base58Address("027151ca63e271b1e637392b37050f9b989417844bf4262eb3a78b1ab1247872ba"));
        addrs.add(ECKey.pubKey2Base58Address("023277981818047207a5487591842cbc0f087a6f7d3fa0d8f1f3cf7c35b38bac71"));
        addrs.add(ECKey.pubKey2Base58Address("03db15ebf22d4c997e189d684782739ca517b078d5890b2f87bd091a0641a9e1b3"));

        //构造交易
        Transaction minerJoinTx = buildMinerJoinTx(list, addrs, 0, (short) 0, BigDecimal.ONE);
        //发送交易
        TransactionCASTxCreaterHttp.sendTx(minerJoinTx, ip, port);

    }

    public static Transaction buildMinerJoinTx(List<UTXO> utxos, List<String> addrs, long lockTime, short version, BigDecimal fee) {
        if (CollectionUtils.isEmpty(addrs)) {
            throw new RuntimeException("addrs is empty");
        }
        if (!AmountUtils.check(false, fee)) {
            throw new RuntimeException("fee is null or more than less 0");
        }
        if (0L > lockTime) {
            throw new RuntimeException("lockTime is less 0");
        }
        if (0 > version) {
            throw new RuntimeException("version is less 0");
        }

        String address = ECKey.pubKey2Base58Address(ECKey.pubKey2Base58Address(pubKey));
        Transaction minerTx = new Transaction();
        minerTx.setVersion(version);
        minerTx.setLockTime(lockTime);

        if (CollectionUtils.isEmpty(utxos)) {
            throw new RuntimeException("address = " + address + " utxos is empty.");
        }

        //The amount of the remaining transaction costs
        BigDecimal feeAssets = new BigDecimal("0");
        //The remaining amount of mining interest
        BigDecimal minerAssets = new BigDecimal("0");

        List<TransactionInput> inputLists = Lists.newArrayList();

        for (UTXO utxo : utxos) {
            if (null != utxo && utxo.isCASCurrency()) {
                //input
                feeAssets = feeAssets.add(utxo.getOutput().getAmount());
                TransactionInput feeInput = StopOrRestartMineTxCreaterHttp.buildTransactionInput(utxo);
                if (null == feeInput) {
                    throw new RuntimeException("feeInput is null");
                }
                inputLists.add(feeInput);
            }
            //The mining interests of a MINER are determined by the number of MINER COINS, and only when the MINER >= 1, has the right to dig.
            if (null != utxo && utxo.isMinerCurrency()) {
                //input
                minerAssets = minerAssets.add(utxo.getOutput().getAmount());
                TransactionInput minerInput = StopOrRestartMineTxCreaterHttp.buildTransactionInput(utxo);
                if (null == minerInput) {
                    throw new RuntimeException("minerInput is null");
                }
                inputLists.add(minerInput);
            }
        }

        if (0 > feeAssets.compareTo(fee)) {
            throw new RuntimeException("fee is not enough");
        }
        if (0 > minerAssets.compareTo(new BigDecimal(addrs.size()))) {
            throw new RuntimeException("miner amount is not enough");
        }
        minerTx.setInputs(inputLists);

        //output
        List outputs = Lists.newArrayList();
        for (String addr : addrs) {
            if (!ECKey.checkBase58Addr(addr)) {
                continue;
            }
            if (0 > minerAssets.compareTo(MINER_AMOUNT)) {
                throw new RuntimeException("The balance in the address is insufficient.");
            }
            TransactionOutput minerOutput = StopOrRestartMineTxCreaterHttp.generateTransactionOutput(addr, SystemCurrencyEnum.MINER, MINER_AMOUNT);
            if (null != minerOutput) {
                minerAssets = minerAssets.subtract(MINER_AMOUNT);
                outputs.add(minerOutput);
            }
        }
        if (CollectionUtils.isEmpty(outputs)) {
            throw new RuntimeException("output is empty");
        }

        //back MINER change
        if (0 < minerAssets.compareTo(BigDecimal.ZERO)) {
            TransactionOutput minerRest = StopOrRestartMineTxCreaterHttp.generateTransactionOutput(address, SystemCurrencyEnum.MINER, minerAssets);
            if (null != minerRest) {
                outputs.add(minerRest);
            }
        }
        //back CAS change
        if (0 < feeAssets.compareTo(fee)) {
            TransactionOutput casRest = StopOrRestartMineTxCreaterHttp.generateTransactionOutput(address, SystemCurrencyEnum.CAS, feeAssets.subtract(fee));
            if (null != casRest) {
                outputs.add(casRest);
            }
        }
        minerTx.setOutputs(outputs);

        //Generate the signature of the transaction
        UpdateAddressTxCreaterHttp.signAndSetUnLockScript(inputLists, minerTx, priKey, pubKey);

        return minerTx;
    }

}
