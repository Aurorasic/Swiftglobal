package com.higgsblock.global.chain.app.blockchain.transaction;

import com.higgsblock.global.chain.common.utils.Money;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.Map;

/**
 * sort transaction result
 * @author tangkun
 * @date 2018-05-24
 */
@Getter
@Setter
@AllArgsConstructor
public class SortResult {

    private boolean overrun;

    private Map<String, Money> feeMap;
}
