package cn.primeledger.cas.global.blockchain.transaction;

import com.google.common.base.Charsets;
import com.google.common.hash.Hashing;
import lombok.Data;
import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.util.Strings;

/**
 * @author baizhengwen
 * @create 2018-03-06
 **/
@Data
public class MinerOutput extends BaseOutput {

    /**
     * the amount of cas coin to spend
     */
    private String address;

    @Override
    public String getHash() {
        return Hashing.sha256().hashString(null == address ? Strings.EMPTY : address, Charsets.UTF_8).toString();
    }

    @Override
    public boolean valid() {
        if (StringUtils.isBlank(address)) {
            return false;
        }
        return super.valid();
    }
}