package com.pankovdv.raft;

import com.pankovdv.raft.context.Context;
import com.pankovdv.raft.node.neighbour.NeighbourService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@Slf4j
public class RaftApplication implements ApplicationRunner {

    @Autowired
    private Context context;
    @Autowired
    private NeighbourService neighbourService;

    public static void main(String[] args) {
        SpringApplication.run(RaftApplication.class, args);
    }

    @Override
    public void run(ApplicationArguments args) {
        // Отправляем сообщение после старта приложения
        log.info("I'm alive. My port - " + System.getenv("server.port"));
        log.info("Context: state - " + context.getState() + ", id - " + context.getId());
        neighbourService.updateNeighbours();
    }
}