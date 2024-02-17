package com.pankovdv.raft.network;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
@ConfigurationProperties(prefix = "network")
public class NetworkProperties {

    private List<Service> services;

    public List<Service> getOtherServices() {
        return services.stream()
                .filter(service -> !service.id.equals(Integer.valueOf(System.getenv("id"))))
                .collect(Collectors.toList());
    }

    public Service getMe() {
        return services.stream()
                .filter(service -> service.id.equals(Integer.valueOf(System.getenv("id"))))
                .findFirst()
                .orElse(null);
    }

    public void setServices(List<Service> services) {
        this.services = services;
    }

    @Data
    public static class Service {
        private Integer id;
        private String host;
        private int port;
        private String version;
    }
}