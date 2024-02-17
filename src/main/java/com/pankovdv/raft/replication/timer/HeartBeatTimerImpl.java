package com.pankovdv.raft.replication.timer;

import com.pankovdv.raft.CustomTimer;
import com.pankovdv.raft.node.State;
import com.pankovdv.raft.replication.ReplicationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class HeartBeatTimerImpl extends CustomTimer {

    @Autowired
    private ReplicationService replicationService;

    @Override
    protected Integer getTimeout() {
        return properties.getHeartbeatTimeout();
    }

    @Override
    protected String getActionName() {
        return "REPLICATION";
    }

    @Override
    protected Runnable getAction() {
        return replicationService::appendRequest;
    }

    @Override
    protected boolean isRun() {
        return context.getState().equals(State.LEADER);
    }
}
