package com.pankovdv.raft.journal.storage;

import com.pankovdv.raft.context.Context;
import com.pankovdv.raft.journal.operation.Entry;
import com.pankovdv.raft.journal.operation.Operation;
import com.pankovdv.raft.journal.operation.OperationsLog;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
class StorageServiceImpl implements StorageService {

    private final Storage storage;
    private final OperationsLog operationsLog;
    private final Context context;

    public Integer lastApplied = -1;

    @Override
    public String get(Integer id) {
        return storage.get(id);
    }

    @Override
    public List<Entry> all() {
        return storage.all();
    }

    @Override
    public void applyCommitted() {
        log.debug("Peer #{} Trying to apply, Commit Index: {} ", context.getId(),
                context.getCommitIndex());
        while (lastApplied < context.getCommitIndex()) {
            apply(context.getCommitIndex());
        }
    }

    private void apply(Integer index) {
        Operation operation = operationsLog.get(index);
        Entry entry = operation.getEntry();
        log.info("Peer #{} Apply operation to storage: {} key: {} value: {} ", context.getId(), operation.getType(), entry.getKey(), entry.getVal());

        switch (operation.getType()) {
            case INSERT -> storage.insert(entry.getKey(), entry.getVal());
            case UPDATE -> storage.update(entry.getKey(), entry.getVal());
            case DELETE -> storage.delete(entry.getKey());
            default -> throw new RuntimeException("Unsupported operation");
        }
        lastApplied++;
    }
}
