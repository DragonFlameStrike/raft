package com.pankovdv.raft.election.dto;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class ElectionRequestDto {

    private final Long term;

    private final Integer candidateId;

    private final Integer lastLogIndex;

    private final Long lastLogTerm;
}
