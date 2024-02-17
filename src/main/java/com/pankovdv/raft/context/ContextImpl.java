package com.pankovdv.raft.context;

import com.pankovdv.raft.journal.Journal;
import com.pankovdv.raft.network.NetworkProperties;
import com.pankovdv.raft.node.State;
import com.pankovdv.raft.node.neighbour.Neighbour;
import com.pankovdv.raft.node.term.Term;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Setter
@Slf4j
public class ContextImpl implements Context {

    @Autowired
    private Journal journal;

    @Autowired
    private NetworkProperties networkProperties;

    @Autowired
    private Term term;

    volatile State currState;
    volatile Integer votedFor;

    public ContextImpl() {
        this.currState = State.FOLLOWER;
    }

    @Override
    public Integer getId() {
        return networkProperties.getMe().getId();
    }

    @Override
    public Term getTerm() {
        return this.term;
    }

    @Override
    public State getState() {
        return this.currState;
    }

    @Override
    public void setState(State state) {
        this.currState = state;
    }

    @Override
    public Integer getVotedFor() {
        return this.votedFor;
    }

    @Override
    public void setVotedFor(Integer id) {
        this.votedFor = id;
    }

    @Override
    public Long getCurrentJournalTerm() {
        return journal.getLastTerm();
    }

    @Override
    public Integer getCommitIndex() {
        return journal.getLastIndex();
    }

    @Override
    public List<Neighbour> getNeighbours() {
        return null;
    }

    @Override
    public void setTermGreaterThenCurrent(Long term) {
        log.info("Node #{} Get term {} greater then current. The current term is {}", getId(), term, getCurrentJournalTerm());
        setState(State.FOLLOWER);
        getTerm().setCurrentTerm(term, getId());
        setVotedFor(null);
    }
}
