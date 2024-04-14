package com.pankovdv.raft.context;

import com.pankovdv.raft.node.State;
import com.pankovdv.raft.node.neighbour.Neighbour;
import com.pankovdv.raft.node.term.Term;

import java.util.List;

public interface Context {
    Integer getId();
    Term getTermService();
    State getState();
    void setState(State state);
    Integer getVotedFor(); //Идентификатор узла за который был отдан голос в текущем раунде
    void setVotedFor(Integer id);
    Long getCurrentJournalTerm();
    Integer getCommitIndex();
    List<Neighbour> getNeighbours();
    void setTermGreaterThenCurrent(Long term);

    void setCommitIndex(int n);
}
