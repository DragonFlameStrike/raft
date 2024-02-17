package com.pankovdv.raft.node.neighbour;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

@RequiredArgsConstructor
@Getter
@Setter
public class Neighbour {

    private final Integer id;
    private final Integer port;
    private AtomicInteger nextIndex = new AtomicInteger(0);
    private AtomicInteger matchIndex = new AtomicInteger(-1);
    private AtomicBoolean voteGranted = new AtomicBoolean(false);

    public void decNextIndex() {
        this.nextIndex.decrementAndGet();
    }
}