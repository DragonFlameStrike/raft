package com.pankovdv.raft.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "raft")
@Data
public class RaftProperties {

    private Integer heartbeatTimeout;
    private Integer voteEvaluateTimeout;
}
