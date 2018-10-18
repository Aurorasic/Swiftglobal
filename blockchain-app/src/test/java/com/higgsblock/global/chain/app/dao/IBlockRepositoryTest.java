package com.higgsblock.global.chain.app.dao;

import com.higgsblock.global.chain.app.BaseTest;
import com.higgsblock.global.chain.app.contract.RepositoryRoot;
import com.higgsblock.global.chain.app.service.impl.UTXOServiceProxy;
import com.higgsblock.global.chain.vm.core.SystemProperties;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author tangkun
 * @date 2018-10-16
 */
@Slf4j
public class IBlockRepositoryTest extends BaseTest {

    @Autowired
    private IBlockRepository blockRepository;

    @Autowired
    private UTXOServiceProxy utxoServiceProxy;

    @Autowired
    private IContractRepository contractRepository;

    @Test
    public void testFindByHeight() throws Exception {
        LOGGER.info("block:{}", blockRepository.findByHeight(2L));
    }

    @Test
    public void testKey() {
        //LOGGER.info("key:{}", );
//        contractRepository.findAll().stream().forEach(item -> {
//            LOGGER.info("key:{} \n value:{}", item.getKey(), item.getValue());
//        });

//        utxoServiceProxy.getUnionUTXO("8cfc501305e295ed1be769f70e2325d58e543f6cd5b123de2b650fca7603922b",
//                "1GnMSCb2hj9fCdfdwk2iuP1iEWXq3ZgH1g", SystemCurrencyEnum.CAS.getCurrency()).stream().forEach(item -> {
//            LOGGER.info("utxo:{}", item);
//        });
//        String key = "d844bb55167ab332117049e240166fc521d67c478ef6cb56ffb4bda2787ff5e7";
//        LOGGER.info("value:{}", SerializationUtils.deserialize(Hex.decode(contractRepository.findOne(key).getValue())));
//
//        LOGGER.info("account:{}", Hex.toHexString(HashUtil.sha3("account".getBytes())));
//        byte[] key = AddrUtil.toContractAddr("1GnMSCb2hj9fCdfdwk2iuP1iEWXq3ZgH1g");
//        String str = Hex.toHexString(ByteUtil.xorAlignRight(key, HashUtil.sha3("account".getBytes())));
//        LOGGER.info("str:{}", str);


        //String preBlockHash, UTXOServiceProxy utxoServiceProxy, SystemProperties config
        RepositoryRoot blockRepository = new RepositoryRoot(contractRepository, "ea68f559a9cd7d11117b2faccb7a7fd3ff83ffcf96b9f888947aec51dbb51291", utxoServiceProxy, SystemProperties.getDefault());
        LOGGER.info("utxos:{}", blockRepository.getUnSpendAsset("1DqLTPwmJXzdKT3nSi3vwQ4VuivrKcfjoE"));
    }

    @Test
    public void testUtxo() {

    }

}