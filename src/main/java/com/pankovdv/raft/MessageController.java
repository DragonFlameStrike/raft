package com.pankovdv.raft;

import com.pankovdv.raft.election.ElectionService;
import com.pankovdv.raft.election.dto.ElectionRequestDto;
import com.pankovdv.raft.election.dto.ElectionResponseDto;
import com.pankovdv.raft.election.timer.ElectionTimerImpl;
import com.pankovdv.raft.node.neighbour.NeighbourService;
import com.pankovdv.raft.node.neighbour.dto.BroadcastRequestDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1")
@Slf4j
public class MessageController {

    @Autowired
    private ElectionTimerImpl electionTimer;
    @Autowired
    private ElectionService electionService;
    @Autowired
    private NeighbourService neighbourService;

    @PostMapping("/broadcast")
    public ResponseEntity<String> broadcast(@RequestBody BroadcastRequestDto requestDto) {
        neighbourService.addNewNeighbour(requestDto.getId(),requestDto.getPort());
        return ResponseEntity.ofNullable("I'm here!");
    }

    @PostMapping("/vote-for-me")
    public ResponseEntity<ElectionResponseDto> receiveMyVote(@RequestBody ElectionRequestDto requestDto) {
        electionTimer.reset();
        return ResponseEntity.ofNullable(electionService.vote(requestDto));
    }

    @GetMapping("/replication")
    public void replication() {
        log.info("Replication received!");
        electionTimer.reset();
    }
}