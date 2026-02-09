package com.theblood.springfood.client.utils;

import io.grpc.Channel;
import io.grpc.ManagedChannelBuilder;

import java.util.concurrent.TimeUnit;

public class ClientUtils {

    public static String DEFAULT_DEFAULT_LOAD_BALANCING_POLICY = "round_robin";

    private ClientUtils() {
    }

    public static String getTarget(String serviceName) {
        return ClientConstants.GRPC_CONFIG_JSON_OBJECT.getJSONObject(serviceName).getString("target");
    }

    public static String getMode(String serviceName) {
        return ClientConstants.GRPC_CONFIG_JSON_OBJECT.getJSONObject(serviceName).getString("mode");
    }

    public static Channel createChannel(Service service) {
        return ManagedChannelBuilder.forTarget(getTarget(service.toString()))
                .usePlaintext()
                .defaultLoadBalancingPolicy(DEFAULT_DEFAULT_LOAD_BALANCING_POLICY)
                .idleTimeout(60, TimeUnit.SECONDS)
                .build();
    }
}
