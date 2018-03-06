package cn.primeledger.cas.global.blockchain.handler;

import cn.primeledger.cas.global.blockchain.Block;
import cn.primeledger.cas.global.blockchain.BlockService;
import cn.primeledger.cas.global.blockchain.formatter.BlockFormatter;
import cn.primeledger.cas.global.common.formatter.IEntityFormatter;
import cn.primeledger.cas.global.common.handler.BaseEntityHandler;
import cn.primeledger.cas.global.consensus.BlockTimer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @author baizhengwen
 * @date 2018/2/28
 */
@Component("blockHandler")
@Slf4j
public class BlockHandler extends BaseEntityHandler<Block> {

    @Autowired
    private BlockTimer blockTimer;

    private IEntityFormatter<Block> formatter = new BlockFormatter();
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
        blockTimer.time(data);
        blockService.persistBlockAndIndex(data);
        blockService.broadCastBlock(data);
    }
}
