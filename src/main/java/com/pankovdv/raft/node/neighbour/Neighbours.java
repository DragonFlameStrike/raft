package com.pankovdv.raft.node.neighbour;

import jakarta.annotation.PostConstruct;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@RequiredArgsConstructor
public class Neighbours {

    private Integer quorum;
    private final List<Neighbour> neighbours = new ArrayList<>();

    public void add(Integer id, Integer port) {
        neighbours.add(new Neighbour(id, port));
    }

    public void clear() {
        neighbours.clear();
    }

    public Neighbour get(Integer id) {
        return neighbours.stream().
                filter(neighbour -> neighbour.getId().equals(id)).
                findFirst().
                orElse(null);
    }

    void updateNeighbours() {
        this.quorum = (neighbours.size() + 1) / 2 + 1;
    }
}
