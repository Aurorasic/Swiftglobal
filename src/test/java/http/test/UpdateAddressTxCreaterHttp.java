package http.test;

import cn.primeledger.cas.global.blockchain.transaction.*;
import cn.primeledger.cas.global.crypto.ECKey;
import cn.primeledger.cas.global.script.UnLockScript;
import cn.primeledger.cas.global.utils.AmountUtils;
import com.google.common.collect.Lists;
import org.apache.commons.collections4.CollectionUtils;

import java.math.BigDecimal;
import java.util.List;

/**
 * @author Su Jiulong
 * @date 2018/3/12
 */
public class UpdateAddressTxCreaterHttp {

    //private key
    private static String priKey = "a525251d56f20a921cda6cd9d71511d4a0edb02e3a0783445e3ab96e100a3daa";
    //public key
    private static String pubKey = "0377b85fbc137825bac7d933faf7b9807579c62afaf2cd462cc471a1ea2b14ed90";
    //new address
    private static String otherAddress = ECKey.pubKey2Base58Address("03bc7747eb46b1f3ae64e087c80e97a7137206ef8e0cf940ee47200a87d9ef1d2d");

    public static Transaction buildUpdateAddressTx(List<UTXO> utxos, String newAddress, long lockTime, short version, BigDecimal fee) {
        if (!ECKey.checkBase58Addr(newAddress)) {
            throw new RuntimeException("Incorrect address format.");
        }
        if (!AmountUtils.check(false, fee)) {
            throw new RuntimeException("fee is null or more than less 0 or more than maximum");
        }
        if (lockTime < 0L) {
            throw new RuntimeException("The lockTime more than less 0");
        }
        if (version < 0) {
            throw new RuntimeException("The version more than less 0");
        }

        Transaction minerTx = new Transaction();
        minerTx.setVersion(version);
        minerTx.setLockTime(lockTime);
        //根据指定的地址获取对应的UTXOS
//        List<UTXO> utxos = utxoMap.values().stream().filter(utxo -> StringUtils.equals(address, utxo.getAddress())).collect(Collectors.toList());

        if (CollectionUtils.isEmpty(utxos)) {
            throw new RuntimeException("The utxos of address is empty.");
        }
        //Transfer fee
        BigDecimal casFee = new BigDecimal("0");
        //Mining rights
        BigDecimal minerFee = new BigDecimal("0");
        //Community management coin
        BigDecimal communityFee = new BigDecimal("0");
        //Issue Token Stake
        BigDecimal issueTokeStake = new BigDecimal("0");

        List<TransactionInput> inputs = Lists.newArrayList();
        TransactionInput feeInput = null;
        TransactionInput minerInput = null;
        TransactionInput communityInput = null;
        TransactionInput issueTokenStakeInput = null;

        for (UTXO utxo : utxos) {
            if (null == utxo) {
                continue;
            }
            if (utxo.isCASCurrency() && utxo.getOutput().getAmount().compareTo(BigDecimal.ZERO) > 0) {
                //Collect the UTXO of the CAS currency.
                casFee = casFee.add(utxo.getOutput().getAmount());
                feeInput = StopOrRestartMineTxCreaterHttp.buildTransactionInput(utxo);
                inputs.add(feeInput);
            }
            if (utxo.isMinerCurrency() && utxo.getOutput().getAmount().compareTo(BigDecimal.ZERO) > 0) {
                //Collect the UTXO of the MINER currency.
                minerFee = minerFee.add(utxo.getOutput().getAmount());
                minerInput = StopOrRestartMineTxCreaterHttp.buildTransactionInput(utxo);
                inputs.add(minerInput);
            }
            if (utxo.isCommunityManagerCurrency() && utxo.getOutput().getAmount().compareTo(BigDecimal.ZERO) > 0) {
                //Collect the UTXO of the COMMUNITY_MANAGER currency.
                communityFee = communityFee.add(utxo.getOutput().getAmount());
                communityInput = StopOrRestartMineTxCreaterHttp.buildTransactionInput(utxo);
                inputs.add(communityInput);
            }
            if (utxo.isIssueTokenCurrency() && utxo.getOutput().getAmount().compareTo(BigDecimal.ZERO) > 0) {
                //Collect the UTXO of the ISSUE_TOKEN currency.
                issueTokeStake = issueTokeStake.add(utxo.getOutput().getAmount());
                issueTokenStakeInput = StopOrRestartMineTxCreaterHttp.buildTransactionInput(utxo);
                inputs.add(issueTokenStakeInput);
            }
        }

        if (CollectionUtils.isEmpty(inputs)) {
            throw new RuntimeException("There is no UTXO available.");
        }
        minerTx.setInputs(inputs);

        //output
        List outputList = Lists.newArrayList();

        TransactionOutput minerOutputCAS2newAddress = StopOrRestartMineTxCreaterHttp.generateTransactionOutput(newAddress, SystemCurrencyEnum.CAS, casFee.subtract(fee));
        if (minerOutputCAS2newAddress != null) {
            outputList.add(minerOutputCAS2newAddress);
        }
        //Transfer all MINER coins to the new address.
        TransactionOutput minerOutputMINER2NewAddress = StopOrRestartMineTxCreaterHttp.generateTransactionOutput(newAddress, SystemCurrencyEnum.MINER, minerFee);
        if (minerOutputMINER2NewAddress != null) {
            outputList.add(minerOutputMINER2NewAddress);
        }

        TransactionOutput minerOutputCommunity2NewAddress = StopOrRestartMineTxCreaterHttp.generateTransactionOutput(newAddress, SystemCurrencyEnum.COMMUNITY_MANAGER, communityFee);
        if (minerOutputCommunity2NewAddress != null) {
            outputList.add(minerOutputCommunity2NewAddress);
        }

        TransactionOutput minerOutputIssueTokenStake2NewAddress = StopOrRestartMineTxCreaterHttp.generateTransactionOutput(newAddress, SystemCurrencyEnum.ISSUE_TOKEN, issueTokeStake);
        if (minerOutputIssueTokenStake2NewAddress != null) {
            outputList.add(minerOutputIssueTokenStake2NewAddress);
        }

        if (CollectionUtils.isEmpty(outputList)) {
            return null;
        }
        minerTx.setOutputs(outputList);
        signAndSetUnLockScript(inputs, minerTx, priKey, pubKey);
        return minerTx;
    }

    /**
     * Set the signature and public key to the unlock script.
     *
     * @param inputs
     * @param transaction
     * @param priKey
     * @param pubKey
     */
    public static void signAndSetUnLockScript(List<TransactionInput> inputs, Transaction transaction, String priKey, String pubKey) {
        String signMessage = ECKey.signMessage(transaction.getHash(), priKey);
        inputs.forEach(transactionInput -> {
            UnLockScript unLockScript = transactionInput.getUnLockScript();
            if (unLockScript != null) {
                unLockScript.getPkList().add(pubKey);
                unLockScript.getSigList().add(signMessage);
            }
        });
    }

    public static void main(String[] args) throws Exception {
        String ip = "localhost";
        int port = 8081;

        //Gets the corresponding UTXO according to the specified address.
        List<UTXO> list = TransactionCASTxCreaterHttp.getUTXOSByAddress(ECKey.pubKey2Base58Address(pubKey), ip, port);

        //build transaction
        Transaction updateAddressTx = buildUpdateAddressTx(list, otherAddress, 0L, (short) 0, new BigDecimal(0.5));

        System.out.println(list.stream().toString());

        //发送交易
        TransactionCASTxCreaterHttp.sendTx(updateAddressTx, ip, port);

    }
}
