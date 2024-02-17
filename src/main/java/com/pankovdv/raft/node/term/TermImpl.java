package com.pankovdv.raft.node.term;

import com.pankovdv.raft.context.Context;
import com.pankovdv.raft.network.NetworkProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.concurrent.atomic.AtomicLong;


@Component
@Slf4j
@RequiredArgsConstructor
public class TermImpl implements Term {

    private final AtomicLong currentTerm = new AtomicLong(0L);

    @Override
    public Long getCurrentTerm() {
        return currentTerm.get();
    }

    @Override
    public void setCurrentTerm(long currentTerm, Integer id) {
        this.currentTerm.set(currentTerm);
        log.info("Node #{} Set term to {}", id, getCurrentTerm());
    }

    @Override
    public Long incCurrentTerm(Integer id) {
        currentTerm.incrementAndGet();
        log.info("Node #{} Term incremented: {}", id, getCurrentTerm());
        return getCurrentTerm();
    }
}