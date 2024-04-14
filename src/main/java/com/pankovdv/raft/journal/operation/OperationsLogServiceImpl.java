package com.pankovdv.raft.journal.operation;

import com.pankovdv.raft.context.Context;
import com.pankovdv.raft.node.State;
import com.pankovdv.raft.node.term.Term;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

import static com.pankovdv.raft.journal.operation.OperationType.*;

@Service
@RequiredArgsConstructor
@Slf4j
class OperationsLogServiceImpl implements OperationsLogService {

    private final OperationsLog operationsLog;
    private final Term term;
    private final Context context;

    @Override
    public void insert(Entry entry) {
        appendToLog(INSERT, entry);
    }

    @Override
    public void update(Integer key, String val) {
        appendToLog(UPDATE, new Entry(key, val));
    }

    @Override
    public void delete(Integer key) {
        appendToLog(DELETE, new Entry(key, null));
    }

    @Override
    public List<Operation> all() {
        return operationsLog.all();
    }

    private void appendToLog(OperationType type, Entry entry) {

        if (!context.getState().equals(State.LEADER)) {
            log.info("appendToLog error. Not a leader");
            return;
        }
        Operation operation = new Operation(term.getCurrentTerm(), type, entry);
        operationsLog.append(operation);
        //applicationEventPublisher.publishEvent(new OperationsLogAppendedEvent(this));
    }
}
