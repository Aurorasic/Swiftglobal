package cn.primeledger.cas.global.blockchain;

import cn.primeledger.cas.global.Application;
import cn.primeledger.cas.global.blockchain.transaction.*;
import cn.primeledger.cas.global.crypto.ECKey;
import cn.primeledger.cas.global.crypto.model.KeyPair;
import cn.primeledger.cas.global.script.LockScript;
import cn.primeledger.cas.global.script.UnLockScript;
import cn.primeledger.cas.global.utils.AmountUtils;
import cn.primeledger.cas.global.utils.ExecutorServices;
import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutorService;
import java.util.stream.Collectors;

/**
 * @author yuguojia
 * @date 2018/03/08
 **/
@Slf4j
@Component
public class PreMiningService {

    private final static BigDecimal AMOUNT = new BigDecimal("1000000");

    private final static BigDecimal MINER_AMOUNT = new BigDecimal("1.0");
    /**
     * 该私钥为超级社区矿机，用来创建创世快
     * peer.priKey=6f297284275fe7d774977dd79d20496b3b8fc0405f64f28033842da74403ecb5
     * peer.pubKey=024d2913d1390e5fcb74567291fe1cb3f7e53bac1fda5703e16df0b9df1fbc5e38
     * peer.addr=1P453kHG2nb9P8LebFih1uXyLxU5D1GLwr
     */
    public static ECKey GENESIS_ECKEY = ECKey.fromPrivateKey("94c3a68199dcb226104d4aeace181350f6a9ea45a356ae641a2147f4c7d9cbf0");
    /**
     * mining blocks of height are:2,3,4,5...
     */
    public static String[] PRE_BLOCK_PKS = new String[]{

            //jiantao
//            "028a186b944c76d7ca626a3ba8ba9609d46de318affb48ee760a0c3336f426d741"
            "03dac408737ba931026936b7420b7a72afedf358822e49436848a4b664362a833e"
            , "02fa7f10fc794de37151738a1aadd95d3000206292b8b36fc1698e323c5543726d"
            , "02560157ed444430b566494bf1f22e269b7874f2ca285e38dabf43c9bf41fa24e2"
            , "0264c3d0e59862f9df7a5d8d9d812761a383559cb7ff1489ed16a12425e2224b13"
//            , "02c3a0dae0b88758065981fa2f7c666123671cac621cb725f9cb6ca6418439d32a"
//            , "03fabdc8f16a108e59453f6b658f47b9e158f0e0755bc04553b994a364fc7312ac"

            //yangyi
//            , "02ae5f432cec2b7e19bd5fb58fc98934f4b50a38a8376063f3bdb7a6f6291559a5"
//            , "02f9d158b8227bed46d916454be5cd2140d0b3e4d1f569a7c542f27e44d5ba4d43"
//            , "02a7aaeb38529b367c24f9d255cbc072ef2d339af0ebe71ae9723cc89f2f56acb2"
//            , "0330b29542bf6a0ea4bb9b99118d5ee50998031ec6ac2ba3fc0fc03f6b24c621b4"
//            , "02704d378d26a178fde9d79a0227052b62be3b8c3e50827b1adea364c059b09683"
            //jiulong
            , "0377b85fbc137825bac7d933faf7b9807579c62afaf2cd462cc471a1ea2b14ed90"
            , "03faab97fae96d4c492dd1bc0764c5a96a8b582c6ca4b41a583de1367b15d95812"
            , "031107ce9ca6db21b8732893873a0a5afb8f393601148acce9402bbab8562709a7"
            , "0367a2279fc0910c3feca555461ddda7f9173f74da99e454fcc2f36d0bb4feff6a"
            , "02a7c81cd3fe3ff0c05b7a3f87642d9aefa1386d8e58b09f613b25587be098b3d4"
//            , "028dd824e120b070edc7436e2757cab81ca587b8992ec09a7b204e3774c9b876ac"
//            , "02be14f156c60150e20ba865ed0e5189747c26154354f3d548c9ecb8a39264c65d"
//            , "03e2576529b8e999a551e9ba46ad391b35200b3e4a7485fdc1e5322d9167bf7b48"

            //yuguojia
//            , "024d2913d1390e5fcb74567291fe1cb3f7e53bac1fda5703e16df0b9df1fbc5e38"
//            , "03d5eb9f503d18e9c5d998e4dd9a3b5e43b7dccfa20957c7ee32ede8117d1a1a10"
//            , "0310d0395c023e37e8f68173391909c72d1730ba674fc0ca891bf9a358ba10855a"
//            , "03ac1c3501d70879ffd62b618ba79bd1b6938ed29a1ee7dadc5a3ffd95f7f7237e"
//            , "023e16858a397dc443f36a850735a7f735d3113e09a2353371f72ec72b6ca4bb32"

            //kongyu
            , "03db15ebf22d4c997e189d684782739ca517b078d5890b2f87bd091a0641a9e1b3"
            , "020b358c19b623c4fd5d3b38ac126c6f798b4d60eec1db39a2dfb035b96c734350"
            , "03c59ffcdf8a7155544a06b4cc0a5ad87824e959cec404a465ead672a12ec69a17"
            , "03aea965d1106f7a2927b62ad59d96aa8731b33eb07b2a6346fd2451b0cca2ba7e"
            , "03abcc6467ec25ea2f5a29b976cbf7df50e5d0c45be55ed781e3b3c8c9b687a976"


            //bailaoshi
//                , "023277981818047207a5487591842cbc0f087a6f7d3fa0d8f1f3cf7c35b38bac71"
//                , "03191f4ee28ca5cb585880cd32ef7723c7b16ed9847bc6c2996267bad60c9423e6"
//                , "027151ca63e271b1e637392b37050f9b989417844bf4262eb3a78b1ab1247872ba"
//                , "034d628b469d5ec0458e1f9aaa587ccba1140242fe0b6501a7a0b2d44a56767556"
//                , "02628d6c7ca1d7272dc81d9165d274e3291deca02ff992e7747a1ae246a7097be5"
//                , "031e00c3b505a82e9f3b913da8677e0ace9d59fa50064454155457e162c03bca7e"
//                , "03bf0a736382220cd3509ec96e833e072ca907b3856fc676fa737cc7dbaa6b481a"

            //zhao xiaogang
//            , "037758be1e9d961bd40fff7e657dfac82d71d07280a594a60fcf188958314ae444"

            // liu weizhen
//            , "02e67329a60a7fdaa7ef6c119175ff6d870c84501008a4c9bb953053a35c4ad2f8"
//            , "0343fd9e602f4f2f3cc9955b484e1378ac2a092a64d6f7c678d840883eddc5971d"

    };
    private static List<String> COMMUN_ADDRS = Lists.newArrayList();
    @Resource(name = "blockData")
    private ConcurrentMap<String, Block> blockMap;
    @Resource(name = "blockIndexData")
    private ConcurrentMap<Long, BlockIndex> blockIndexMap;
    @Autowired
    private TransactionService transactionService;
    @Autowired
    private KeyPair peerKeyPair;
    @Autowired
    private BlockService blockService;
    @Autowired
    private TransactionCacheManager txCacheManager;
    @Resource(name = "utxoData")
    private ConcurrentMap<String, UTXO> utxoMap;

    public static void main(String[] arg) {
        for (int i = 1; i < 8; i++) {
            ECKey ecKey = new ECKey();
            System.out.println(ecKey.getKeyPair().getPriKey());
            System.out.println(ecKey.getKeyPair().getPubKey());
            System.out.println(ECKey.pubKey2Base58Address(ecKey.getKeyPair().getPubKey()));
            System.out.println();
        }

        System.out.println(ECKey.pubKey2Base58Address("03db15ebf22d4c997e189d684782739ca517b078d5890b2f87bd091a0641a9e1b3"));

        boolean pair = ECKey.checkPriKeyAndPubKey("1954b19a2f78e1a1b5a42bdc042e66a671152cc7a3ccab40b1bca14685a6d962",
                "03db15ebf22d4c997e189d684782739ca517b078d5890b2f87bd091a0641a9e1b3");
        System.out.println(pair);
    }

    public boolean initGenesisBlocks() {
        Set set = blockMap.keySet();
        if (set.size() < 1) {
            return genesisBlock();
        }
        return true;
    }

    /**
     * 创建创世区块
     *
     * @return
     */
    public boolean genesisBlock() {
        //社区的矿工挖矿地址写死
        COMMUN_ADDRS.add(ECKey.pubKey2Base58Address(GENESIS_ECKEY.getKeyPair().getPubKey()));
        for (String pk : PRE_BLOCK_PKS) {
            COMMUN_ADDRS.add(ECKey.pubKey2Base58Address(pk));
        }

        List<Transaction> transactions = Lists.newLinkedList();
        Transaction communityTx = buildCommunityMinerTx(COMMUN_ADDRS, 0, (short) 0);
        if (null != communityTx) {
            transactions.add(communityTx);
        }
        if (CollectionUtils.isEmpty(transactions)) {
            return false;
        }

        Block block = new Block();
        block.setVersion((short) 1);
        block.setBlockTime(0);
        block.setPrevBlockHash(null);
        block.setTransactions(transactions);
        block.setHeight(1);
        block.setNodes(COMMUN_ADDRS);
        block.setPubKey(GENESIS_ECKEY.getKeyPair().getPubKey());
        String sig = ECKey.signMessage(block.getHash(), GENESIS_ECKEY.getKeyPair().getPriKey());
        block.initMinerPkSig(GENESIS_ECKEY.getKeyPair().getPubKey(), sig);
        blockService.persistBlockAndIndex(block, null, (short) 0);
        //donot need broadcast
        return true;
    }

    public void preMiningBlocks() {
        String myPubKey = peerKeyPair.getPubKey();

        //mining 2-->13 height blocks
        if (PRE_BLOCK_PKS.length < Application.PRE_BLOCK_COUNT - 1) {
            throw new RuntimeException("pre blocking address count is error.");
        }
        for (int i = 2; i <= Application.PRE_BLOCK_COUNT; i++) {
            if (StringUtils.equals(myPubKey, PRE_BLOCK_PKS[i - 2])) {
                asynPackageOneBlock(myPubKey, i - 1);
            }
        }
    }

    public Transaction buildCoinBasePreMine(ECKey ecKey, BigDecimal amount, String address, long lockTime, short version) {
        if (amount == null) {
            throw new RuntimeException("please set the amount");
        }
        if (!AmountUtils.check(false, amount)) {
            throw new RuntimeException("the amount need more than zero");
        }
        if (!ECKey.checkBase58Addr(address)) {
            throw new RuntimeException("the address format is not correct");
        }
        if (ecKey == null) {
            throw new RuntimeException("the ecKey can not be blank");
        }
        Transaction transaction = new Transaction();
        transaction.setVersion(version);
        transaction.setMinerPubKey(ecKey.getKeyPair().getPubKey());
        transaction.setLockTime(lockTime);

        TransactionOutput transactionOutput = new TransactionOutput();
        transactionOutput.setAmount(amount);
        transactionOutput.setCurrency(SystemCurrencyEnum.CAS.getCurrency());
        LockScript lockScript = new LockScript();
        lockScript.setAddress(address);
        transactionOutput.setLockScript(lockScript);
        List emptyList = Lists.newArrayList();
        emptyList.add(transactionOutput);
        transaction.setOutputs(emptyList);
        transaction.setPubKeyAndSignPair(new PubKeyAndSignPair(ecKey.getKeyPair().getPubKey()
                , ECKey.signMessage(transaction.getHash(), ecKey.getKeyPair().getPriKey())));
        return transaction;
    }

    private void asynPackageOneBlock(String pubKey, long preHeight) {
        ExecutorService executorService = ExecutorServices.newSingleThreadExecutor("preMining", 1);
        executorService.submit((Runnable) () -> {
            while (true) {
                try {
                    Thread.sleep(1000);
                    BlockIndex lastBlockIndex = blockService.getLastBlockIndex();
                    long lastBlockHeight = lastBlockIndex.getHeight();
                    if (lastBlockHeight < preHeight) {
                        LOGGER.warn("my last block height {} is too small, do not to pre mining", lastBlockHeight);
                    }
                    if (lastBlockHeight == preHeight) {
                        LOGGER.info("asynPackageOneBlock, preHeight:" + lastBlockIndex.getHeight());
                        packagePreBlock(pubKey);
                        executorService.shutdownNow();
                        return;
                    }
                    if (lastBlockHeight > preHeight) {
                        executorService.shutdownNow();
                        return;
                    }
                } catch (InterruptedException e) {
                    LOGGER.error("pre mining error", e);
                }
            }
        });
    }

    private void packagePreBlock(String pubKey) {
        List<String> addresses = new ArrayList<>();
        addresses.add(ECKey.pubKey2Base58Address(pubKey));
        Transaction transaction = buildMinerJoinTx(addresses, 0L, (short) 1, BigDecimal.ONE);
        txCacheManager.addTransaction(transaction);
        Block block = blockService.packageNewBlock();
        blockService.persistBlockAndIndex(block, null, (short) 0);
        blockService.broadCastBlock(block);
    }

    public Transaction buildMinerJoinTx(List<String> addrs, long lockTime, short version, BigDecimal fee) {
        if (CollectionUtils.isEmpty(addrs)) {
            throw new RuntimeException("addrs is empty");
        }
        if (!AmountUtils.check(false, fee)) {
            throw new RuntimeException("fee is null or more than less 0");
        }
        if (null == peerKeyPair) {
            throw new RuntimeException("peerKeyPair is null");
        }
        if (0L > lockTime) {
            throw new RuntimeException("lockTime is less 0");
        }
        if (0 > version) {
            throw new RuntimeException("version is less 0");
        }

        String address = ECKey.pubKey2Base58Address(peerKeyPair.getPubKey());
        Transaction minerTx = new Transaction();
        minerTx.setMinerPubKey(peerKeyPair.getPubKey());
        minerTx.setVersion(version);
        minerTx.setLockTime(lockTime);

        List<UTXO> utxos = utxoMap.values().stream().filter(utxo -> StringUtils.equals(address, utxo.getAddress())).collect(Collectors.toList());
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
                TransactionInput feeInput = biuldTransactionInput(utxo);
                if (null == feeInput) {
                    throw new RuntimeException("feeInput is null");
                }
                inputLists.add(feeInput);
            }
            //The mining interests of a MINER are determined by the number of MINER COINS, and only when the MINER >= 1, has the right to dig.
            if (null != utxo && utxo.isMinerCurrency()) {
                //input
                minerAssets = minerAssets.add(utxo.getOutput().getAmount());
                TransactionInput minerInput = biuldTransactionInput(utxo);
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
            TransactionOutput minerOutput = buildTransactionOutput(addr, SystemCurrencyEnum.MINER, MINER_AMOUNT);
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
            TransactionOutput minerRest = buildTransactionOutput(address, SystemCurrencyEnum.MINER, minerAssets);
            if (null != minerRest) {
                outputs.add(minerRest);
            }
        }
        //back CAS change
        if (0 < feeAssets.compareTo(fee)) {
            TransactionOutput casRest = buildTransactionOutput(address, SystemCurrencyEnum.CAS, feeAssets.subtract(fee));
            if (null != casRest) {
                outputs.add(casRest);
            }
        }
        minerTx.setOutputs(outputs);

        //Build the signature of the transaction
        Transaction resultTx = buildTransactionSignature(minerTx, peerKeyPair);
        if (null == resultTx) {
            throw new RuntimeException("build sign error");
        }
        return resultTx;
    }

    private Transaction buildTransactionSignature(Transaction tx, KeyPair peerKeyPair) {
        if (null == tx || null == peerKeyPair) {
            return null;
        }
        Transaction transaction = tx;
        String signatrue = ECKey.signMessage(tx.getHash(), peerKeyPair.getPriKey());
        transaction.getInputs().forEach(transactionInput -> {
            UnLockScript unLockScript = transactionInput.getUnLockScript();
            if (unLockScript != null) {
                unLockScript.getPkList().add(peerKeyPair.getPubKey());
                unLockScript.getSigList().add(signatrue);
            }
        });
        return transaction;
    }

    private TransactionInput biuldTransactionInput(UTXO utxo) {
        if (null == utxo) {
            return null;
        }
        TransactionInput intput = new TransactionInput();
        TransactionOutPoint feeOutPoint = new TransactionOutPoint();
        UnLockScript unlockScript = new UnLockScript();
        unlockScript.setPkList(Lists.newArrayList());
        unlockScript.setSigList(Lists.newArrayList());
        feeOutPoint.setHash(utxo.getHash());
        feeOutPoint.setIndex(utxo.getIndex());
        intput.setPrevOut(feeOutPoint);
        intput.setUnLockScript(unlockScript);
        return intput;
    }

    public Transaction buildCommunityMinerTx(List<String> addrs, long lockTime, short version) {
        if (CollectionUtils.isEmpty(addrs)) {
            throw new RuntimeException("the addresss is empty");
        }
        if (null == peerKeyPair) {
            throw new RuntimeException("peerKeyPair is null");
        }
        if (0L > lockTime) {
            throw new RuntimeException("lockTime is less 0");
        }
        if (0 > version) {
            throw new RuntimeException("version is less 0");
        }

        Transaction transaction = new Transaction();
        transaction.setMinerPubKey(peerKeyPair.getPubKey());
        transaction.setVersion(version);
        transaction.setLockTime(lockTime);
        List outputLists = Lists.newArrayList();

        for (String address : addrs) {
            if (!ECKey.checkBase58Addr(address)) {
                continue;
            }
            TransactionOutput output1 = buildTransactionOutput(address, SystemCurrencyEnum.CAS, AMOUNT);
            if (null != output1) {
                outputLists.add(output1);
            }
            TransactionOutput output2 = buildTransactionOutput(address, SystemCurrencyEnum.COMMUNITY_MANAGER, AMOUNT);
            if (null != output2) {
                outputLists.add(output2);
            }
            TransactionOutput output3 = buildTransactionOutput(address, SystemCurrencyEnum.MINER, AMOUNT);
            if (null != output3) {
                outputLists.add(output3);
            }
        }
        if (CollectionUtils.isEmpty(outputLists)) {
            throw new RuntimeException("output is empty");
        }
        transaction.setOutputs(outputLists);

        //The way the transaction is signed without input
        transaction.setPubKeyAndSignPair(new PubKeyAndSignPair(peerKeyPair.getPubKey(), ECKey.signMessage(transaction.getHash(), peerKeyPair.getPriKey())));
        return transaction;
    }

    private TransactionOutput buildTransactionOutput(String address, SystemCurrencyEnum currencyEnum, BigDecimal amount) {
        if (!ECKey.checkBase58Addr(address)) {
            return null;
        }
        if (null == currencyEnum) {
            return null;
        }
        if (!AmountUtils.check(false, amount)) {
            return null;
        }

        TransactionOutput output = new TransactionOutput();
        output.setAmount(amount);
        output.setCurrency(currencyEnum.getCurrency());
        LockScript lockScript = new LockScript();
        lockScript.setAddress(address);
        output.setLockScript(lockScript);
        return output;
    }
}