package com.theblood.springfood.chat.service;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.springframework.messaging.simp.user.SimpUserRegistry;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Service for managing chat-related metrics using Micrometer.
 * 
 * Metrics:
 * - Counters: chat.messages.sent, chat.messages.delivered, chat.messages.persisted,
 *             chat.auth.failures, chat.kafka.errors, chat.redis.errors
 * - Gauges: chat.websocket.connections (active WebSocket connections)
 * - Timers: chat.message.latency, chat.persistence.latency
 * 
 * Requirements: All
 */
@Service
public class ChatMetricsService {

    private final MeterRegistry meterRegistry;
    private final SimpUserRegistry userRegistry;

    // Counters
    private final Counter messagesSentCounter;
    private final Counter messagesDeliveredCounter;
    private final Counter messagesPersistedCounter;
    private final Counter authFailuresCounter;
    private final Counter kafkaErrorsCounter;
    private final Counter redisErrorsCounter;

    // Timers
    private final Timer messageLatencyTimer;
    private final Timer persistenceLatencyTimer;

    public ChatMetricsService(MeterRegistry meterRegistry, SimpUserRegistry userRegistry) {
        this.meterRegistry = meterRegistry;
        this.userRegistry = userRegistry;

        // Initialize counters
        this.messagesSentCounter = Counter.builder("chat.messages.sent")
            .description("Total messages sent via WebSocket")
            .register(meterRegistry);

        this.messagesDeliveredCounter = Counter.builder("chat.messages.delivered")
            .description("Total messages delivered to clients")
            .register(meterRegistry);

        this.messagesPersistedCounter = Counter.builder("chat.messages.persisted")
            .description("Total messages saved to database")
            .register(meterRegistry);

        this.authFailuresCounter = Counter.builder("chat.auth.failures")
            .description("Authentication failures")
            .register(meterRegistry);

        this.kafkaErrorsCounter = Counter.builder("chat.kafka.errors")
            .description("Kafka producer/consumer errors")
            .register(meterRegistry);

        this.redisErrorsCounter = Counter.builder("chat.redis.errors")
            .description("Redis connection failures")
            .register(meterRegistry);

        // Initialize timers
        this.messageLatencyTimer = Timer.builder("chat.message.latency")
            .description("Time from send to receive")
            .register(meterRegistry);

        this.persistenceLatencyTimer = Timer.builder("chat.persistence.latency")
            .description("Time from Kafka to database")
            .register(meterRegistry);

        // Initialize gauge for active WebSocket connections
        Gauge.builder("chat.websocket.connections", userRegistry, this::getActiveConnectionCount)
            .description("Active WebSocket connections")
            .register(meterRegistry);
    }

    /**
     * Get the count of active WebSocket connections.
     * 
     * @param registry The SimpUserRegistry
     * @return The number of active connections
     */
    private int getActiveConnectionCount(SimpUserRegistry registry) {
        return registry.getUserCount();
    }

    /**
     * Increment the messages sent counter.
     */
    public void incrementMessagesSent() {
        messagesSentCounter.increment();
    }

    /**
     * Increment the messages delivered counter.
     */
    public void incrementMessagesDelivered() {
        messagesDeliveredCounter.increment();
    }

    /**
     * Increment the messages persisted counter.
     * 
     * @param count The number of messages persisted (for batch operations)
     */
    public void incrementMessagesPersisted(int count) {
        messagesPersistedCounter.increment(count);
    }

    /**
     * Increment the authentication failures counter.
     */
    public void incrementAuthFailures() {
        authFailuresCounter.increment();
    }

    /**
     * Increment the Kafka errors counter.
     */
    public void incrementKafkaErrors() {
        kafkaErrorsCounter.increment();
    }

    /**
     * Increment the Redis errors counter.
     */
    public void incrementRedisErrors() {
        redisErrorsCounter.increment();
    }

    /**
     * Record message latency (time from send to receive).
     * 
     * @param startTime The message creation timestamp
     */
    public void recordMessageLatency(Instant startTime) {
        Duration duration = Duration.between(startTime, Instant.now());
        messageLatencyTimer.record(duration);
    }

    /**
     * Record persistence latency (time from Kafka to database).
     * 
     * @param startTime The Kafka message timestamp
     */
    public void recordPersistenceLatency(Instant startTime) {
        Duration duration = Duration.between(startTime, Instant.now());
        persistenceLatencyTimer.record(duration);
    }

    /**
     * Get the MeterRegistry for custom metrics.
     * 
     * @return The MeterRegistry
     */
    public MeterRegistry getMeterRegistry() {
        return meterRegistry;
    }
}
