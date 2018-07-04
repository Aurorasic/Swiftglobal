package com.higgsblock.global.chain.app.context;

import com.google.common.eventbus.EventBus;
import com.higgsblock.global.chain.app.blockchain.BlockService;
import com.higgsblock.global.chain.app.blockchain.CandidateMiner;
import com.higgsblock.global.chain.app.blockchain.WitnessCountTime;
import com.higgsblock.global.chain.app.common.handler.IEntityHandler;
import com.higgsblock.global.chain.app.connection.ConnectionManager;
import com.higgsblock.global.chain.app.consensus.syncblock.SyncBlockService;
import com.higgsblock.global.chain.app.schedule.BaseTask;
import com.higgsblock.global.chain.common.eventbus.listener.IEventBusListener;
import com.higgsblock.global.chain.network.PeerManager;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * @author baizhengwen
 * @date 2018/3/22
 */
@Component
public class AppContext {

    @Autowired
    private BlockService blockService;

    @Autowired
    private ConnectionManager connectionManager;

    @Autowired
    private PeerManager peerManager;

    @Autowired
    private List<BaseTask> tasks;

    @Autowired
    private List<IEntityHandler> entityHandlers;

    @Autowired
    private SyncBlockService syncBlockService;

    @Autowired
    private List<IEventBusListener> eventBusListeners;

    @Autowired
    private EventBus eventBus;

    @Autowired
    private CandidateMiner candidateMiner;

    @Autowired
    private WitnessCountTime witnessCountTime;

    public void start() throws Exception {
        checkAndRecoveryBlockData();

        startHandlers();

        startListeners();

        startSocketServer();

        loadSelfPeerInfo();

        loadOrFetchPeers();

        startPeerTimerTasks();

        syncBlocks();

        startCandidateCountTime();

        startWitnessCountTime();

    }

    private void checkAndRecoveryBlockData() {
        blockService.loadAllBlockData();
    }

    private void startHandlers() {
        if (CollectionUtils.isNotEmpty(entityHandlers)) {
            entityHandlers.forEach(IEntityHandler::start);
        }
    }

    private void startListeners() {
        CollectionUtils.forAllDo(eventBusListeners, eventBus::register);
    }

    private void startSocketServer() {
        connectionManager.startServer();
    }

    private void loadSelfPeerInfo() {
        peerManager.loadSelfPeerInfo();
    }

    private void loadOrFetchPeers() {
        peerManager.loadNeighborPeers();
        connectionManager.connectToPeers(1, 5, 20);
    }

    private void startPeerTimerTasks() {
        if (CollectionUtils.isNotEmpty(tasks)) {
            tasks.forEach(BaseTask::start);
        }
    }

    private void syncBlocks() {
        syncBlockService.startSyncBlock();
    }

    private void startCandidateCountTime() throws InterruptedException {
//        CandidateMiner candidateMiner = new CandidateMiner();
        candidateMiner.queryCurrHeightStartTime();
    }

    private void startWitnessCountTime() throws InterruptedException {
        //WitnessCountTime witnessCountTime = new WitnessCountTime();
        witnessCountTime.queryCurrHeightStartTime();
    }

}
