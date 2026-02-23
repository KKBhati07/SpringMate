package com.example.SpringMate.Util;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

@Slf4j
@Component
public class EndpointPrinter implements CommandLineRunner {

    private final RequestMappingHandlerMapping appHandlerMapping;
    private final RequestMappingHandlerMapping actuatorHandlerMapping;

    public EndpointPrinter(
            @Qualifier("requestMappingHandlerMapping")
            RequestMappingHandlerMapping appHandlerMapping,

            @Qualifier("controllerEndpointHandlerMapping")
            RequestMappingHandlerMapping actuatorHandlerMapping
    ) {
        this.appHandlerMapping = appHandlerMapping;
        this.actuatorHandlerMapping = actuatorHandlerMapping;
    }

    @Override
    public void run(String... args) {

        log.info("========== APPLICATION ENDPOINTS ==========");
        appHandlerMapping.getHandlerMethods()
                .forEach((mapping, handler) ->
                        log.debug("{} -> {}", mapping, handler.getMethod().toGenericString())
                );

        log.info("========== ACTUATOR ENDPOINTS ==========");
        actuatorHandlerMapping.getHandlerMethods()
                .forEach((mapping, handler) ->
                        log.debug("{} -> {}", mapping, handler.getMethod().toGenericString())
                );
    }
}
