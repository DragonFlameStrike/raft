package com.pankovdv.raft.journal.operation;

import lombok.Getter;
import lombok.RequiredArgsConstructor;


@RequiredArgsConstructor
@Getter
public class Entry {

    private final Integer key;
    private final String val;
}
