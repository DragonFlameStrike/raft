package com.pankovdv.raft.election.timer;

import com.pankovdv.raft.CustomTimer;
import com.pankovdv.raft.election.ElectionService;
import com.pankovdv.raft.node.State;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ElectionTimerImpl extends CustomTimer {

    @Autowired
    private ElectionService electionService;

    @Override
    protected Integer getTimeout() {
        return properties.getVoteEvaluateTimeout();
    }

    @Override
    protected String getActionName() {
        return "GOLOSOVANIE";
    }

    @Override
    protected Runnable getAction() {
        return electionService::processElection;
    }

    @Override
    protected boolean isRun() {
        return context.getState().equals(State.FOLLOWER);
    }
}