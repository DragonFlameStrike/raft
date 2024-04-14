package com.pankovdv.raft.journal.storage;

import com.pankovdv.raft.journal.operation.Entry;

import java.util.List;

public interface Storage {

    List<Entry> all();

    String get(Integer key);

    void insert(Integer key, String val);

    void update(Integer key, String val);

    void delete(Integer key);
}
