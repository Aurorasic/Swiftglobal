package com.higgsblock.global.chain.app.vm;

import com.higgsblock.global.chain.app.blockchain.transaction.Transaction;
import com.higgsblock.global.chain.app.blockchain.transaction.TransactionOutput;
import com.higgsblock.global.chain.app.blockchain.transaction.UTXO;
import com.higgsblock.global.chain.app.contract.Helpers;
import com.higgsblock.global.chain.app.contract.RepositoryRoot;
import com.higgsblock.global.chain.app.utils.AddrUtil;
import com.higgsblock.global.chain.vm.core.Repository;
import com.higgsblock.global.chain.vm.core.SystemProperties;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

/**
 * @author tangkun
 * @date 2018-09-07
 */
public class SnapshotTest {

    @Test
    public void testUTXO() {


        //一级缓存
        Repository parent = new RepositoryRoot(null, "", null, SystemProperties.getDefault());
        //二级缓存
        Repository txR = parent.startTracking();
        //三级缓存
        Repository conR = txR.startTracking();
        byte[] from = AddrUtil.toContractAddr("1LZ88bckco6XZRywsLEEgbDtin2wPWGZxV");
        String amount = "10", currency = "cas";
        //conR.transfer(from,"1LZ88bckco6XZRywsLEEgbDtin2wPWGZx2",BigInteger.valueOf(10),currency);
        //conR.transfer(from,"1LZ88bckco6XZRywsLEEgbDtin2wPWGZx3",BigInteger.valueOf(20),currency);

        System.out.println(conR.getAccountState(from, currency).getBalance());

        //检查input>=outputs

//        List<UTXO>
        Transaction internalTx = Helpers.buildContractTransaction(Helpers.buildTestUTXO("" + from),
                conR.getAccountState(from, currency), conR.getAccountDetails(), null, null);


        int outputSize = internalTx.getOutputs().size();
        List<UTXO> unSpendUTXO = new ArrayList<>(outputSize);
        for (int i = 0; i < outputSize; i++) {
            TransactionOutput output = internalTx.getOutputs().get(i);
            UTXO utxo = new UTXO(internalTx, (short) i, output);
            unSpendUTXO.add(utxo);
        }

        //  conR.mergeUTXO(Helpers.buildTestUTXO("" + from), unSpendUTXO);

        conR.flush();
        txR.flush();

//        parent.getUnSpendAsset("1LZ88bckco6XZRywsLEEgbDtin2wPWGZxV").forEach(item -> System.out.println(((UTXO)item).getOutput().getMoney().getValue()));
//        parent.getSpendAsset("1LZ88bckco6XZRywsLEEgbDtin2wPWGZxV").forEach(item -> System.out.println(((UTXO)item).getOutput().getMoney().getValue()));
//
//        parent.getUnSpendAsset("1LZ88bckco6XZRywsLEEgbDtin2wPWGZx2").forEach(item -> System.out.println(((UTXO)item).getOutput().getMoney().getValue()));
//        parent.getUnSpendAsset("1LZ88bckco6XZRywsLEEgbDtin2wPWGZx3").forEach(item -> System.out.println(((UTXO)item).getOutput().getMoney().getValue()));


        //刷新


        //是否重复
//        if(spentUTXOCache.getOrDefault(contractAddress,new ArrayList<>()).containsAll(chainUTXO)){
//            //双花
//        }
//        spentUTXOCache.getOrDefault(contractAddress,new ArrayList<>()).addAll(chainUTXO);


    }


}


