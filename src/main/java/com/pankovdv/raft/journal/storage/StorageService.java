package com.pankovdv.raft.journal.storage;


import com.pankovdv.raft.journal.operation.Entry;

import java.util.List;

public interface StorageService {

    String get(Integer id);

    List<Entry> all();


    void applyCommitted();
}
