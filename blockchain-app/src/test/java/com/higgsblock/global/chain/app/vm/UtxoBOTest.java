package com.higgsblock.global.chain.app.vm;

import com.higgsblock.global.chain.vm.core.UTXOBO;
import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Set;

/**
 * @author tangkun
 * @date 2018-09-10
 */
@Slf4j
public class UtxoBOTest {

    public static void main(String[] args) {
//String txHash, Integer index, BigDecimal value, String address, Integer state

        UTXOBO utxo1 = new UTXOBO("1",1,new BigDecimal("2"),"0xcc",0);
        UTXOBO utxo2 = new UTXOBO("2",1,new BigDecimal("2"),"0xcc",0);
        UTXOBO utxo3 = new UTXOBO("3",1,new BigDecimal("2"),"0xcc",0);
        UTXOBO utxo4 = new UTXOBO("4",1,new BigDecimal("2"),"0xcc",0);


        Set<UTXOBO> set = new HashSet<>();
        set.add(utxo1);
        set.add(utxo2);
        set.add(utxo3);
        set.add(utxo4);


      //  UTXOBO utxo11 = new UTXOBO("1",1,new BigDecimal("2"),"0xcc",0);
        //UTXOBO utxo22 = new UTXOBO("2",1,new BigDecimal("2"),"0xcc",0);
        UTXOBO utxo33 = new UTXOBO("3",1,new BigDecimal("2"),"0xcc",0);
        UTXOBO utxo44 = new UTXOBO("4",1,new BigDecimal("2"),"0xcc",1);
        Set<UTXOBO> set1 = new HashSet<>();
        //set1.add(utxo11);
        //set1.add(utxo22);
        set1.add(utxo33);
        set1.add(utxo44);


        set.addAll(set1);


        System.out.println(utxo33.equals(utxo44));


    }
}
