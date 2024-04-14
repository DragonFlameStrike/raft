package com.pankovdv.raft.replication.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
@AllArgsConstructor
public class ReplicationResponseDto {

    Integer id;

    Long term;

    Boolean success;

    Integer matchIndex;
}
