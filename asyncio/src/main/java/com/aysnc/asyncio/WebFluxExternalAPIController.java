package com.aysnc.asyncio;



import org.springframework.http.MediaType;
        import org.springframework.web.reactive.function.client.WebClient;
        import org.springframework.web.bind.annotation.GetMapping;
        import org.springframework.web.bind.annotation.RestController;
        import reactor.core.publisher.Mono;

@RestController
public class WebFluxExternalAPIController {

    private final WebClient webClient;

    public ExternalAPIController(WebClient.Builder webClientBuilder) {
        this.webClient = webClientBuilder.baseUrl("https://ipinfo.io").build();
    }

    @GetMapping(value = "/data", produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<String> getData() {
        return webClient.get()
                .uri("/161.185.160.93/geo")
                .retrieve()
                .bodyToMono(String.class);
    }
}

