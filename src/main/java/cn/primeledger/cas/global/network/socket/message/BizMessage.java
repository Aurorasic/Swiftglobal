package cn.primeledger.cas.global.network.socket.message;

import com.google.common.base.Charsets;
import com.google.common.hash.Hashing;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Biz message includes all the business layer messages as a message set.
 *
 * @author yuanjiantao
 * @date Created in 3/1/2018
 */

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BizMessage extends BaseMessage {

    private String data;

    public String getHash() {
        return Hashing.goodFastHash(128).newHasher()
                .putString(data, Charsets.UTF_8)
                .hash()
                .toString();
    }
}
