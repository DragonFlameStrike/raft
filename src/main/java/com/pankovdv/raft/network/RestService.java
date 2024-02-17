package com.pankovdv.raft.network;

import org.springframework.http.ResponseEntity;

public interface RestService {

    String sendGetRequest(String url);

    <T> ResponseEntity<T> sendPostRequest(String url, Object payload, Class<T> respType);

    String buildUrl(Integer port, String path);
}
