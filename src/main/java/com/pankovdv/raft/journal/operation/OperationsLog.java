package com.pankovdv.raft.journal.operation;

import java.util.List;

public interface OperationsLog {

    void append(Operation operation);
    Operation get(Integer index);

    List<Operation> all();

    Long getTerm(Integer index);
    Integer getLastIndex();
    Long getLastTerm();

    void removeAllFromIndex(int newOperationIndex);

    void log();
}
