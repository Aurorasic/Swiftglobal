package cn.primeledger.cas.global.consensus.sign.handler;

import cn.primeledger.cas.global.blockchain.Block;
import cn.primeledger.cas.global.blockchain.BlockService;
import cn.primeledger.cas.global.common.formatter.IEntityFormatter;
import cn.primeledger.cas.global.common.handler.BaseEntityHandler;
import cn.primeledger.cas.global.consensus.sign.formatter.CollectSignFormatter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;


/**
 * @author zhao xiaogang
 * @date 2018/3/6
 */
@Component("collectSignHandler")
@Slf4j
public class CollectSignHandler extends BaseEntityHandler<Block> {

    private IEntityFormatter<Block> formatter = new CollectSignFormatter();

    @Autowired
    private BlockService blockService;

    @Override
    protected IEntityFormatter<Block> getEntityFormatter() {
        return formatter;
    }

    @Override
    protected void doProcess(Block data) {
        if (!blockService.valid(data)) {
            LOGGER.error("error block: " + data);
            return;
        }
        blockService.persistBlockAndIndex(data);
    }
}
