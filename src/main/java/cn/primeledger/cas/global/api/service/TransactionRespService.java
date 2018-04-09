package cn.primeledger.cas.global.api.service;

import cn.primeledger.cas.global.blockchain.Block;
import cn.primeledger.cas.global.blockchain.BlockIndex;
import cn.primeledger.cas.global.blockchain.listener.MessageCenter;
import cn.primeledger.cas.global.blockchain.transaction.*;
import cn.primeledger.cas.global.crypto.ECKey;
import cn.primeledger.cas.global.script.LockScript;
import cn.primeledger.cas.global.script.UnLockScript;
import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.mapdb.BTreeMap;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ConcurrentNavigableMap;
import java.util.stream.Collectors;

/**
 * @author kongyu
 * @date 2018-03-19
 */
@Slf4j
@Service
public class TransactionRespService {
    @Autowired
    private TransactionService transactionService;

    @Resource(name = "transactionIndexData")
    private ConcurrentMap<String, TransactionIndex> transactionIndexData;

    @Resource(name = "blockIndexData")
    private ConcurrentMap<Long, BlockIndex> blockIndexData;

    @Resource(name = "blockData")
    private ConcurrentMap<String, Block> blockData;

    @Resource(name = "pubKeyMap")
    private BTreeMap<byte[], byte[]> pubKeyMap;

    @Autowired
    private MessageCenter messageCenter;

    /**
     * Send transaction information
     *
     * @param tx
     * @return
     */
    public Boolean sendTransaction(Transaction tx) {
        LOGGER.info("sendTransaction start ...");
        if (null == tx) {
            return false;
        }
        LOGGER.info("transaction is = " + tx.toString());
        messageCenter.accept(tx);
        return true;
    }

    /**
     * According to the transaction hash query corresponding transaction information
     *
     * @param hash
     * @return
     */
    public List<Transaction> getTransactionByTxHash(String hash) {
        if (null == hash) {
            throw new RuntimeException("params is null");
        }
        TransactionIndex transactionIndex = transactionIndexData.get(hash);
        Block block = blockData.get(transactionIndex.getBlockHash());
        List<Transaction> transactions = block.getTransactions().stream().filter(transaction -> StringUtils.equals(hash, transaction.getHash())).collect(Collectors.toList());
        return transactions;
    }

    /**
     * Query the corresponding transaction information according to the public key
     *
     * @param pubKey
     * @param op
     * @return
     */
    public List<Transaction> getTransactionByPubKey(String pubKey, String op) {
        if (null == pubKey || null == op) {
            throw new RuntimeException("params is null");
        }

        List<Block> blocks = Lists.newArrayList(blockData.values());
        if (CollectionUtils.isEmpty(blocks)) {
            return null;
        }

        List<Transaction> resultTxs = Lists.newArrayList();
        List<Transaction> transactions = null;
        //标志是否与pubKey关联
        Boolean flag = false;

        for (Block block : blocks) {
            transactions = block.getTransactions();
            if (CollectionUtils.isEmpty(transactions)) {
                continue;
            }

            for (Transaction transaction : transactions) {
                if ("all".equals(op)) {
                    //默认为与pubkey有关的所有交易
                    flag = checkTransactionInputByPubKey(transaction, pubKey);
                    flag = (flag) ? flag : checkTransactionOutputByPubKey(transaction, pubKey);
                    if (!flag) {
                        continue;
                    }
                } else {
                    flag = ("in".equals(op)) ? checkTransactionInputByPubKey(transaction, pubKey) : checkTransactionOutputByPubKey(transaction, pubKey);
                    if (!flag) {
                        continue;
                    }
                }
                if (flag) {
                    resultTxs.add(transaction);
                }
            }
        }
        return resultTxs;
    }

    /**
     * Detects whether the input part of the transaction information contains the specified public key
     *
     * @param transaction
     * @param publicKey
     * @return
     */
    private boolean checkTransactionInputByPubKey(Transaction transaction, String publicKey) {
        if (null == transaction || null == publicKey) {
            return false;
        }

        //是否与pubkey公钥关联的标志
        boolean flag = false;
        List<TransactionInput> transactionInputs = null;
        UnLockScript unLockScript = null;

        transactionInputs = transaction.getInputs();
        if (CollectionUtils.isEmpty(transactionInputs)) {
            return false;
        }

        for (TransactionInput transactionInput : transactionInputs) {
            unLockScript = transactionInput.getUnLockScript();
            if (null == unLockScript) {
                continue;
            }

            List<String> pubKeyList = unLockScript.getPkList();
            if (CollectionUtils.isEmpty(pubKeyList)) {
                continue;
            }

            for (String pubkey : pubKeyList) {
                if (StringUtils.equals(publicKey, pubkey)) {
                    flag = true;
                    break;
                }
            }

            if (flag) {
                break;
            }
        }//end of for
        return flag;
    }

    /**
     * Detects whether the output part of the transaction information contains the specified public key
     *
     * @param transaction
     * @param publicKey
     * @return
     */
    private boolean checkTransactionOutputByPubKey(Transaction transaction, String publicKey) {
        if (null == transaction || null == publicKey) {
            return false;
        }
        String address = ECKey.fromPublicKeyOnly(publicKey).toBase58Address();
        if (null == address) {
            return false;
        }

        //是否与pubkey公钥关联的标志
        boolean flag = false;
        List<TransactionOutput> transactionOutputs = null;
        LockScript lockScript = null;

        transactionOutputs = transaction.getOutputs();
        if (CollectionUtils.isEmpty(transactionOutputs)) {
            return false;
        }

        for (TransactionOutput transactionOutput : transactionOutputs) {
            lockScript = transactionOutput.getLockScript();
            if (null == lockScript) {
                continue;
            }

            if (StringUtils.equals(address, lockScript.getAddress())) {
                flag = true;
                break;
            }
        }//end of for
        return flag;
    }

    public List<Transaction> getTransactionByPubKeyMap(String pubKey, String op) {
        List<Transaction> result = Lists.newArrayList();
        if (null == pubKey || null == op) {
            throw new RuntimeException("params is null");
        }
        String address = ECKey.pubKey2Base58Address(pubKey);
        if (null == address) {
            throw new RuntimeException("address is null");
        }

        ConcurrentNavigableMap<byte[], byte[]> map = pubKeyMap.prefixSubMap(address.getBytes(), true);
        if (map.isEmpty()) {
            return result;
        }

        //遍历查询到的map数据
        for (byte[] addrKey : map.keySet()) {
            if (null == addrKey) {
                continue;
            }

            String[] addressAndTxHash = new String(addrKey).split(":");
            if (3 != addressAndTxHash.length) {
                continue;
            }

            String blockHash = new String(map.get(addrKey));
            if (null == blockHash) {
                continue;
            }
            Block block = blockData.get(blockHash);
            if (null == block) {
                continue;
            }

            List<Transaction> transactions = block.getTransactions();
            if (CollectionUtils.isEmpty(transactions)) {
                continue;
            }

            for (Transaction tx : transactions) {
                if (null == tx) {
                    continue;
                }
                if (StringUtils.equals(addressAndTxHash[1], tx.getHash())) {
                    result.add(tx);
                }
            }
        }
        return result;
    }
}
