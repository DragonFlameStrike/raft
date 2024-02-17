package com.pankovdv.raft.election;

import com.pankovdv.raft.election.dto.ElectionRequestDto;
import com.pankovdv.raft.election.dto.ElectionResponseDto;
import org.springframework.scheduling.annotation.Async;

public interface ElectionService {

    @Async
    void processElection();

    ElectionResponseDto vote(ElectionRequestDto request);
}
