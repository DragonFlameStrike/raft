package com.pankovdv.raft.journal;

import com.pankovdv.raft.journal.operation.Operation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
@Slf4j
@RequiredArgsConstructor
class JournalImpl implements Journal {

    private static final Integer EMPTY_LOG_LAST_INDEX = -1;

    private final List<Operation> journal = new ArrayList<>();

    @Override
    synchronized public void append(Operation operation) {
        journal.add(operation);
    }

    @Override
    public Operation get(Integer index) {
        return journal.get(index);
    }

    @Override
    public List<Operation> all() {
        return journal;
    }

    @Override
    public Integer getLastIndex() {
        return journal.size() - 1;
    }

    @Override
    public Long getLastTerm() {

        Integer lastIndex = getLastIndex();
        if (lastIndex > EMPTY_LOG_LAST_INDEX) {
            return journal.get(lastIndex).getTerm();
        } else
            return 0L;
    }

    @Override
    public Long getTerm(Integer index) {
        if (index > EMPTY_LOG_LAST_INDEX) {
            return journal.get(index).getTerm();
        } else
            return 0L;
    }
}
