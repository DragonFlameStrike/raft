package com.pankovdv.raft.election.dto;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class ElectionResponseDto {

    private final Integer id;

    private final Long term;

    private final  boolean voteGranted;
}
