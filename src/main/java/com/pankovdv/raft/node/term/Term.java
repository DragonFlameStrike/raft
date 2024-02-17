package com.pankovdv.raft.node.term;

public interface Term {

    Long getCurrentTerm();

    void setCurrentTerm(long currentTerm, Integer id);

    Long incCurrentTerm(Integer id);
}
