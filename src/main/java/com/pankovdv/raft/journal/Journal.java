package com.pankovdv.raft.journal;

import com.pankovdv.raft.journal.operation.Operation;

import java.util.List;

public interface Journal {
    void append(Operation operation);
    Operation get(Integer index);
    List<Operation> all();

    Long getTerm(Integer index);
    Integer getLastIndex();
    Long getLastTerm();
}
