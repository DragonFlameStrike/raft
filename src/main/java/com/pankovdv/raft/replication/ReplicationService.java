package com.pankovdv.raft.replication;

import com.pankovdv.raft.replication.dto.ReplicationRequestDto;
import com.pankovdv.raft.replication.dto.ReplicationResponseDto;
import org.springframework.scheduling.annotation.Async;

public interface ReplicationService {

    @Async
    void appendRequest();

    ReplicationResponseDto append(ReplicationRequestDto request);
}
