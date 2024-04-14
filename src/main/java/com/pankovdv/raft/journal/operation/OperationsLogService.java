package com.pankovdv.raft.journal.operation;

import java.util.List;

public interface OperationsLogService {


    List<Operation> all();

    void insert(Entry entry);

    void update(Integer key, String val);

    void delete(Integer key);
}
