package com.pankovdv.raft.replication;

import com.pankovdv.raft.MessageController;
import com.pankovdv.raft.network.NetworkProperties;
import com.pankovdv.raft.network.RestService;
import com.pankovdv.raft.node.neighbour.Neighbour;
import com.pankovdv.raft.node.neighbour.NeighbourService;
import com.pankovdv.raft.node.neighbour.Neighbours;
import com.pankovdv.raft.replication.dto.ReplicationRequestDto;
import com.pankovdv.raft.replication.dto.ReplicationResponseDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ReplicationServiceImpl implements ReplicationService {

    @Autowired
    private RestService restService;

    @Autowired
    private NetworkProperties networkProperties;

    @Autowired
    private NeighbourService neighbourService;

    @Override
    public void appendRequest() {
        sendReplicationStart();
    }

    @Override
    public ReplicationResponseDto append(ReplicationRequestDto request) {
        return null;
    }

    private void sendReplicationStart() {
        Neighbours neighbours = neighbourService.getNeighbours();

        for (Neighbour neighbour : neighbours.getNeighbours()) {
            restService.sendGetRequest("http://localhost:" + neighbour.getPort() +"/api/v1/replication");
        }
    }
}
