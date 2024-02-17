package com.pankovdv.raft.node.neighbour.dto;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class BroadcastRequestDto {
    Integer id;
    Integer port;
}
