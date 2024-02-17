package com.pankovdv.raft.node.neighbour;

public interface NeighbourService {

    Neighbours getNeighbours();
    Neighbours updateNeighbours();
    void addNewNeighbour(Integer id, Integer port);
    void deleteNeighbour(Neighbour neighbour);
}
