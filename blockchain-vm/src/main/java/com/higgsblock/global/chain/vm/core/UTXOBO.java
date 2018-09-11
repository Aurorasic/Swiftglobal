package com.higgsblock.global.chain.vm.core;

import java.math.BigDecimal;

/**
 * @author tangkun
 * @date 2018-09-10
 */

public class UTXOBO  {

    private String txHash;

    private Integer index;

    private BigDecimal value;

    private String address;

    private Integer state;

    public UTXOBO(String txHash, Integer index, BigDecimal value, String address, Integer state) {
        this.txHash = txHash;
        this.index = index;
        this.value = value;
        this.address = address;
        this.state = state;
    }

    public String getTxHash() {
        return txHash;
    }

    public void setTxHash(String txHash) {
        this.txHash = txHash;
    }

    public Integer getIndex() {
        return index;
    }

    public void setIndex(Integer index) {
        this.index = index;
    }

    public BigDecimal getValue() {
        return value;
    }

    public void setValue(BigDecimal value) {
        this.value = value;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public Integer getState() {
        return state;
    }

    public void setState(Integer state) {
        this.state = state;
    }


}
