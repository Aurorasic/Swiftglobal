package com.higgsblock.global.chain.app.sync.message;

import com.higgsblock.global.chain.app.common.constants.MessageType;
import com.higgsblock.global.chain.app.common.message.Message;
import com.higgsblock.global.chain.common.entity.BaseSerializer;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.commons.collections.CollectionUtils;

import java.util.Set;

/**
 * @author yuanjiantao
 * @date 3/8/2018
 */
@Data
@NoArgsConstructor
@Message(MessageType.INVENTORY)
public class Inventory extends BaseSerializer {

    private int version = 0;

    private long height;

    private Set<String> hashs;

    public Inventory(long height, Set<String> hashs) {
        this.height = height;
        this.hashs = hashs;
    }

    public boolean valid() {
        return height > 0 && CollectionUtils.isNotEmpty(hashs);
    }
}
