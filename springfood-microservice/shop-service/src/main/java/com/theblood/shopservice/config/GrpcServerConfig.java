package com.theblood.shopservice.config;

import com.theblood.shopservice.grpc.ProductValidationService;
import io.grpc.BindableService;
import io.grpc.Server;
import io.grpc.ServerBuilder;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RequiredArgsConstructor
public class GrpcServerConfig {

    private final ProductValidationService productValidationService;

    @Value("${grpc.server.port:9090}")
    private int grpcPort;

    private Server server;

    @PostConstruct
    public void startGrpcServer() {
        try {
            server = ServerBuilder.forPort(grpcPort)
                    .addService((BindableService) productValidationService)
                    .build()
                    .start();

            log.info("gRPC server started on port {}", grpcPort);

            // Add shutdown hook
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                log.info("Shutting down gRPC server");
                stopGrpcServer();
            }));

        } catch (Exception e) {
            log.error("Failed to start gRPC server", e);
            throw new RuntimeException("Failed to start gRPC server", e);
        }
    }

    @PreDestroy
    public void stopGrpcServer() {
        if (server != null) {
            server.shutdown();
            try {
                server.awaitTermination();
            } catch (InterruptedException e) {
                log.error("Error stopping gRPC server", e);
                Thread.currentThread().interrupt();
            }
        }
    }
}