package com.pankovdv.raft.journal.operation;

import lombok.Data;

@Data
public class Operation {

    private final Long term;
    private final OperationType type;
    private final Entry entry;
}
