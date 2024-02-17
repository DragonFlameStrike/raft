package com.pankovdv.raft.node.neighbour;

import com.pankovdv.raft.context.Context;
import com.pankovdv.raft.network.NetworkProperties;
import com.pankovdv.raft.network.RestService;
import com.pankovdv.raft.node.neighbour.dto.BroadcastRequestDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
public class NeighbourServiceImpl implements NeighbourService {

    @Autowired
    private NetworkProperties properties;

    @Autowired
    private RestService restService;

    private final Neighbours neighbours = new Neighbours();

    @Override
    public Neighbours getNeighbours() {
        return this.neighbours;
    }

    @Override
    public Neighbours updateNeighbours() {
        neighbours.clear();
        sendBroadcast();
        neighbours.updateNeighbours();
        return this.neighbours;
    }

    @Override
    public void addNewNeighbour(Integer id, Integer port) {
        log.info("New neighbour - #" + id);
        neighbours.add(id, port);
    }

    @Override
    public void deleteNeighbour(Neighbour neighbour){
        neighbours.getNeighbours().remove(neighbour);
        neighbours.updateNeighbours();
    }

    private void sendBroadcast() {
        List<NetworkProperties.Service> services = properties.getOtherServices();

        for (NetworkProperties.Service service : services) {
            var resp = restService.sendPostRequest(
                    restService.buildUrl(service.getPort(),"/api/v1/broadcast"),
                    BroadcastRequestDto.builder().id(properties.getMe().getId()).port(properties.getMe().getPort()).build(),
                    String.class
            );

            if (resp != null) {
                log.info("New neighbour - #" + service.getId());
                neighbours.add(service.getId(), service.getPort());
            }
        }
    }
}