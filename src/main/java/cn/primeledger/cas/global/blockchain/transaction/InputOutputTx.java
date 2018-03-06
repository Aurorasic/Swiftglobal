package cn.primeledger.cas.global.blockchain.transaction;

import cn.primeledger.cas.global.script.UnLockScript;
import com.google.common.base.Charsets;
import com.google.common.hash.HashFunction;
import com.google.common.hash.Hashing;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.util.Strings;

import java.util.List;

/**
 * @author baizhengwen
 * @create 2018-03-06
 **/
@Data
@NoArgsConstructor
public class InputOutputTx<INPUT extends BaseInput, OUTPUT extends BaseOutput> extends BaseTx {

    /**
     * the sources of current spending
     */
    private List<INPUT> inputs;

    /**
     * transfer to other coin
     */
    private List<OUTPUT> outputs;

    /**
     * unlock script: signature and pk
     */
    private List<UnLockScript> unLockScripts;

    @Override
    public String getHash() {
        if (StringUtils.isBlank(hash)) {
            HashFunction function = Hashing.sha256();
            StringBuilder builder = new StringBuilder();
            builder.append(function.hashInt(version));
            builder.append(function.hashInt(type));
            builder.append(function.hashLong(lockTime));
            builder.append(function.hashString(null == extra ? Strings.EMPTY : extra, Charsets.UTF_8));
            if (CollectionUtils.isNotEmpty(inputs)) {
                inputs.forEach((input) -> {
                    TxOutPoint prevOut = input.getPrevOut();
                    if (prevOut != null) {
                        builder.append(function.hashLong(prevOut.getIndex()));
                        String prevOutHash = prevOut.getHash();
                        builder.append(function.hashString(null == prevOutHash ? Strings.EMPTY : prevOutHash, Charsets.UTF_8));
                    }
                });
            } else {
                builder.append(function.hashInt(0));
            }
            if (CollectionUtils.isNotEmpty(outputs)) {
                outputs.forEach((output) -> builder.append(output.getHash()));
            } else {
                builder.append(function.hashInt(0));
            }
            hash = function.hashString(builder, Charsets.UTF_8).toString();
        }
        return hash;
    }
}