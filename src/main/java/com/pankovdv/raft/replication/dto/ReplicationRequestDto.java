package com.pankovdv.raft.replication.dto;

import com.pankovdv.raft.journal.operation.Operation;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
@AllArgsConstructor
public class ReplicationRequestDto {

    Long term;

    Integer leaderId;

    Integer prevLogIndex;

    Long prevLogTerm;

    Integer leaderCommit;

    Operation operation;
}
