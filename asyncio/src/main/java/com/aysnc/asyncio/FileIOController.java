package com.aysnc.asyncio;

import org.springframework.core.io.buffer.DataBufferFactory;
import org.springframework.core.io.buffer.DefaultDataBufferFactory;
import org.springframework.http.MediaType;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

@RestController
public class FileIOController {
    private final DataBufferFactory dataBufferFactory = new DefaultDataBufferFactory();

    @GetMapping(value = "/file", produces = MediaType.TEXT_PLAIN_VALUE)
    public Mono<String> readFile(ServerWebExchange exchange) {
        File file = new File("input.txt");
        if (!file.exists()) {
            return Mono.error(new IOException("File not found"));
        }
        try {
            byte[] fileContent = Files.readAllBytes(file.toPath());
            return Mono.just(new String(fileContent));
        } catch (IOException e) {
            return Mono.error(e);
        }
    }

    @PostMapping(value = "/file", consumes = MediaType.MULTIPART_FORM_DATA_VALUE, produces = MediaType.TEXT_PLAIN_VALUE)
    public Mono<String> writeFile(@RequestPart("file") FilePart file) {
        try {
            File outputFile = new File("output.txt");
            file.transferTo(outputFile);
            return Mono.just("File saved successfully");
        } catch (Exception e) {
            return Mono.error(e);
        }
    }
}
