package com.pankovdv.raft;

import com.pankovdv.raft.context.Context;
import com.pankovdv.raft.journal.operation.Entry;
import com.pankovdv.raft.journal.operation.OperationsLog;
import com.pankovdv.raft.journal.operation.OperationsLogService;
import com.pankovdv.raft.network.NetworkProperties;
import com.pankovdv.raft.node.State;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/gateway")
@Slf4j
public class GatewayController {

    @Autowired
    Context context;

    @Autowired
    NetworkProperties networkProperties;

    @Autowired
    OperationsLogService operationsLogService;

    @Autowired
    OperationsLog operationsLog;

    @PostMapping("/")
    public ResponseEntity<Boolean> getMessage(@RequestBody String message) {
        log.info("Get new message: {}", message);
        if (context.getState() != State.LEADER) {
            log.info("I'm not a LEADER");
            return ResponseEntity.ofNullable(Boolean.FALSE);
        }

        operationsLogService.insert(new Entry(operationsLog.getLastIndex()+1, message));

        log.info("Message saved");
        operationsLog.log();

        return ResponseEntity.ofNullable(Boolean.TRUE);
    }

    @GetMapping("/get-leader")
    public ResponseEntity<String> getLeader() {
        log.info("getLeader invoke");
        if (context.getState() == State.LEADER) {
            var port = String.valueOf(networkProperties.getMe().getPort());
            log.info("I'm a leader");
            return ResponseEntity.ofNullable(port);
        }
        return ResponseEntity.ofNullable("error");
    }
}
