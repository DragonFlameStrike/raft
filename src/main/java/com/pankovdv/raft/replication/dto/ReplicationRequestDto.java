package com.pankovdv.raft.replication.dto;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class ReplicationRequestDto {

    Long term;
}
