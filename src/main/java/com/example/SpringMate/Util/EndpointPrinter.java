package com.example.SpringMate.Util;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

@Slf4j
@Component
@RequiredArgsConstructor
public class EndpointPrinter implements CommandLineRunner {

    private final RequestMappingHandlerMapping handlerMapping;

    @Override
    public void run(String... args) {
        log.info("Registered endpoints:");
        handlerMapping.getHandlerMethods().forEach((mapping, handler) -> {
            log.debug("{} -> {}", mapping, handler.getMethod().getName());;
        });
    }
}
