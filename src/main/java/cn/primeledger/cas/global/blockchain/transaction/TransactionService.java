package cn.primeledger.cas.global.blockchain.transaction;

import cn.primeledger.cas.global.crypto.ECKey;
import cn.primeledger.cas.global.script.LockScript;
import cn.primeledger.cas.global.script.UnLockScript;

import java.util.List;

/**
 * @author baizhengwen
 * @date Created in 2018/2/24
 */
public class TransactionService {

    public static boolean checkSig(String txHash, LockScript lockScript, UnLockScript unLockScript) {
        short type = lockScript.getType();
        List<String> pkList = unLockScript.getPkList();
        List<String> sigList = unLockScript.getSigList();

        boolean valid = unLockScript.valid();
        if (!valid) {
            return valid;
        }

        if (type == ScriptTypeEnum.P2PKH.getType()) {
            if (sigList.size() != 1 || pkList.size() != 1) {
                return false;
            }
            String pubKey = pkList.get(0);
            String signature = sigList.get(0);
            if (!ECKey.checkPubKeyAndAddr(pubKey, lockScript.getAddress())) {
                return false;
            }
            if (!ECKey.verifySign(txHash, signature, pubKey)) {
                return false;
            }
        } else if (type == ScriptTypeEnum.P2SH.getType()) {
            //TODO verify <P2SH> : hash(<2 pk1 pk2 pk3 3>) = p2sh
            for (int i = 0; i < sigList.size(); i++) {
                String pubKey = pkList.get(i);
                String signature = sigList.get(i);
                if (!ECKey.verifySign(txHash, signature, pubKey)) {
                    return false;
                }
            }
        }
        return false;
    }
}
