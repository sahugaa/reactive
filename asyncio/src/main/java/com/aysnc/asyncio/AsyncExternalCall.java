package com.rest.async.rest;

import org.springframework.http.MediaType;
import org.springframework.scheduling.annotation.Async;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;
import java.util.concurrent.CompletableFuture;

@RestController
public class AsyncExternalCall {

    private final RestTemplate restTemplate;

    public AsyncExternalCall(RestTemplate restTemplate) {


        this.restTemplate = restTemplate;
    }

    @GetMapping(value = "/data", produces = MediaType.APPLICATION_JSON_VALUE)
    public CompletableFuture<String> getData() {
        return getDataAsync();
    }

    @Async
    public CompletableFuture<String> getDataAsync() {
        String result = restTemplate.getForObject("https://ipinfo.io/161.185.160.93/geo", String.class);
        return CompletableFuture.completedFuture(result);
    }
}
